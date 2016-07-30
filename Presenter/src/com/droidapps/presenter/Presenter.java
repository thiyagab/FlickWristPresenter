package com.droidapps.presenter;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Presenter {

	public static void main(String[] args) throws AWTException, IOException {

		ServerSocket serverSocket = new ServerSocket(4321);
		System.out.println("Server waiting at "
				+ InetAddress.getLocalHost().getHostAddress()
				+ " on port: 4321...");
		Socket socket = serverSocket.accept();
		System.out.println("Client connected..");
		Robot robot = new Robot();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		String lineString = null;
		while ((lineString = in.readLine()) != null) {
			System.out.println("Incoming: " + lineString);
			if (lineString.toLowerCase().startsWith("l")) {
				robot.keyPress(KeyEvent.VK_LEFT);
				robot.keyRelease(KeyEvent.VK_LEFT);
			} else if (lineString.toLowerCase().startsWith("r")) {
				robot.keyPress(KeyEvent.VK_RIGHT);
				robot.keyRelease(KeyEvent.VK_RIGHT);
			}
		}

	}

}
