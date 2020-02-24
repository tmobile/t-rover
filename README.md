# t-rover
This project attempts to refine both the hardware and software requirements to make a STEM kit possible for anyone interested in trying to build a self driving rc car.  The end goal is to have an affordable and complete kit that can be used at the high school and middle school levels.  The kit currently cost about $170.  It's unclear how long it may take to complete the kits from start to finish.  Very little background in electronics, programming, and machine learning is assumed.  

Currently we are using an off the shelf rc car from Amazon, an arduino uno r3 kit (elegoo ultimate uno kit), BYO computer, and BYO phone.  It's assumed that almost everyone has a computer and phone.  Right now the project is only tested on an android OS, mainly because a free app called "IP Webcam" is available and provides all functionality required.  There should be an equivalent on the iOS side but we have not be able to look into this option.

We are currently testing feasiblity of building the self driving car with 10 different teams.  There should be a lot of lessons learned and these will be folded back into the project to make it easier and better.

## Image Capture
capture_images.py provide a simple way to display images from the "IP Webcam" app.
Using the Google Play Store Android App "IP Webcam"
use this as a starting point to capture images necessary to train the CNN ML.

## Machine Learning Model
t-rover_cnn.ipynb a jupyter notebook from the previous version of T-racer.  This a foundation to create an ML model.


## arudino interface
rc_motor_comms.ino basic communications mechanism between computer/phone and arudino uno

## Unity FPV
simple first person view using Samsung Gear VR goggles.  
