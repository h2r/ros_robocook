Ros_Robocook
============
Publisher and Subscriber to be used with h2r/baking/HackathonSimplified. More information there.

Installation
------------------------

```
$ sudo apt-get install ros-hydro-rosjava
$ cd ~/catkin_ws/src
$ git clone https://github.com/h2r/ros_robocook
$ cd ~/java_workspace
$ git clone -b hackathon https://github.com/h2r/baking
$ cd baking
$ ant
$ cp h2r-baking ~/catkin_ws/ros_robocook/communication/build/libs
$ cd ~/catkin_ws
$ catkin_make


Running
------------------------
```
$ rosrun communication pubsub com.github.communication.pubsub.PubSub
```
