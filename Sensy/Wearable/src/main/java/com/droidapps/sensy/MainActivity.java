/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.droidapps.sensy;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The main activity with a view pager, containing three pages:<p/>
 * <ul>
 * <li>
 * Page 1: shows a list of DataItems received from the phone application
 * </li>
 * <li>
 * Page 2: shows the photo that is sent from the phone application
 * </li>
 * <li>
 * Page 3: includes two buttons to show the connected phone and watch devices
 * </li>
 * </ul>
 */
public class MainActivity extends Activity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        DataApi.DataListener,
        MessageApi.MessageListener,
        CapabilityApi.CapabilityListener,SensorEventListener {

    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;



    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int mSensorType=Sensor.TYPE_ACCELEROMETER;


    private static final String START_ACTIVITY_PATH = "/start-activity";
    static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        initSensors();
    }

    private void initSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(mSensorType);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onPause() {
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LOGD(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.CapabilityApi.addListener(
                mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);
        discoverNodes();


    }

    @Override
    public void onConnectionSuspended(int cause) {
        LOGD(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + result);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LOGD(TAG, "onDataChanged(): " + dataEvents);
    }

    public void onClicked(View view) {
        switch (view.getId()) {
            case R.id.startaction:
                start();
                break;
            case R.id.stopaction:
                stop();
                break;
            default:
                Log.e(TAG, "Unknown click event registered");
        }
    }

    public void onLeft(View view){
        sendData("LEFT");
    }

    public void onRight(View view){
        sendData("RIGHT");
    }



    private void start() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Toast.makeText(MainActivity.this, "Sensor started", Toast.LENGTH_SHORT).show();
    }

    private void stop() {
        Toast.makeText(MainActivity.this, "Sensor Stopped", Toast.LENGTH_SHORT).show();
        mSensorManager.unregisterListener(this);
    }

    private Collection<String> getNodes() {
        HashSet <String>results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }


    /**
     * Find the connected nodes that provide at least one of the given capabilities
     */
    private void discoverNodes() {

        PendingResult<CapabilityApi.GetAllCapabilitiesResult> pendingCapabilityResult =
                Wearable.CapabilityApi.getAllCapabilities(
                        mGoogleApiClient,
                        CapabilityApi.FILTER_REACHABLE);

        pendingCapabilityResult.setResultCallback(
                new ResultCallback<CapabilityApi.GetAllCapabilitiesResult>() {
                    @Override
                    public void onResult(
                            CapabilityApi.GetAllCapabilitiesResult getAllCapabilitiesResult) {

                        if (!getAllCapabilitiesResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to get capabilities");
                            return;
                        }

                        Map<String, CapabilityInfo> capabilitiesMap =
                                getAllCapabilitiesResult.getAllCapabilities();
                        Set<Node> nodes = new HashSet<>();

                        if (capabilitiesMap.isEmpty()) {
                            processDiscoveredNodes(nodes);
                            return;
                        }
                        for (CapabilityInfo capabilityInfo : capabilitiesMap.values()) {
                            if (capabilityInfo != null) {
                                nodes.addAll(capabilityInfo.getNodes());
                            }
                        }
                        processDiscoveredNodes(nodes);
                    }

                    private void processDiscoveredNodes(Set<Node> nodes) {
                        nodesList.clear();
                        for (Node node : nodes) {
                            nodesList.add(node.getId());
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), DATA_ITEM_RECEIVED_PATH,
                                    "wear connected".getBytes());
                        }
                        LOGD(TAG, "Connected Nodes: " + (nodesList.isEmpty()
                                ? "No connected device was found for the given capabilities"
                                : TextUtils.join(",", nodesList)));
                        String msg;
                        if (!nodesList.isEmpty()) {
                            msg = getString(R.string.connected_nodes,
                                    TextUtils.join(", ", nodesList));
                        } else {
                            msg = getString(R.string.no_device);
                        }
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    List<String> nodesList = new ArrayList<>();

    @Override
    public void onMessageReceived(MessageEvent event) {
        LOGD(TAG, "onMessageReceived: " + event);

    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        discoverNodes();
        LOGD(TAG, "onCapabilityChanged: " + capabilityInfo);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                detechShake(event);
        }

    }


    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;


    private long mShakeTimestamp;
    private int mShakeCount;
    private void detechShake(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // gForce will be close to 1 when there is no movement.
        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            final long now = System.currentTimeMillis();
            // ignore shake events too close to each other (500ms)
            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return;
            }

            // reset the shake count after 3 seconds of no shakes
            if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                mShakeCount = 0;
            }

            mShakeTimestamp = now;
            mShakeCount++;

            sendData("RIGHT");
        }
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    public void sendData(String data){
        for (String nodeId : nodesList) {

            Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, DATA_ITEM_RECEIVED_PATH,
                    data.getBytes());
        }
    }

    public static void LOGD(final String tag, String message) {
//        if (Log.isLoggable(tag, Log.DEBUG)) {
        Log.d(tag, message);
//        }
    }
}