# FlickWristPresenter
How magical it would be, if your presentation move with the flick of your wrist or with a snap of your fingers.

Setup: <br>
1.  A laptop to do the presentation and should be java installed<br>
2.  Android mobile<br>
3.  Android Wear device<br>
4.  All the devices should be connected to the same network.<br>

The project has two parts (actually 3), 

1. The JAVA program presenter runs in a pc or mac or ubuntu or pi, where your presentation is connected
2. The Mobile application Sensy which acts as a broker and connects 'wear' and your 'pc'
3. The Wear Application Sensy, senses hand gestures and send commands to its counterpart in Mobile.

USAGE:

1. Download the Presenter.jar from outputs folder (or build yourself) and run it in the PC. </br>
<quote>java -jar Presenter.jar</quote>

2. Download Sensy.apk from outputs folder (or build yourself) and install it in mobile, and make sure the Wearable app is synced with your Android wear.
3. Enter the IP Address of the PC and click on 'connect to ip'
4. From the options menu, select 'Start Activity', this will start the app in the wear
5. In the wear app, click on Start when you are ready to present, then just shake your hand or snap your finger (any gesture which  creates a 'shake' event). 
6.  The wear senses the 'shake' event and sends command to mobile, the mobile inturn sends the command to the PC via wifi. 
7.  The JAVA program in the PC simulates the 'RIGHT' arrow key event and the presentation moves.


