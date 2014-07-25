/*
 * Copyright (C) 2014 kofarrell.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.ros_robocook.communication;

import java.util.*;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import object_recognition_msgs.*;
import geometry_msgs.*;

/**
 * A simple {@link Publisher} {@link NodeMain}.
 */
public class Talker extends AbstractNodeMain {

  //private List<String> containers;


  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("rosjava/talker");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    final List<String> containers = makeContainerList();
    final List<String> tools = makeToolsList();

    final Publisher<RecognizedObjectArray> publisher =
        connectedNode.newPublisher("chatter", RecognizedObjectArray._TYPE);

    final Publisher<RecognizedObject> objectMaker = 
        connectedNode.newPublisher("a", RecognizedObject._TYPE);

    final Publisher<ObjectType> typeMaker = 
        connectedNode.newPublisher("b", ObjectType._TYPE);

    final Publisher<PoseWithCovarianceStamped> poseCovStampedMaker = 
    connectedNode.newPublisher("c", PoseWithCovarianceStamped._TYPE);

    final Publisher<PoseWithCovariance> poseCovMaker = 
    connectedNode.newPublisher("d", PoseWithCovariance._TYPE);

    final Publisher<Pose> poseMaker = 
    connectedNode.newPublisher("e", Pose._TYPE);

    final Publisher<Point> pointMaker = 
    connectedNode.newPublisher("f", Point._TYPE);


    connectedNode.executeCancellableLoop(new CancellableLoop() {
      private int sequenceNumber;

      public void publishBowlMessage() {
        RecognizedObjectArray message = publisher.newMessage();
        List<RecognizedObject> objects = new ArrayList<RecognizedObject>();

        for (String name : containers) {
          float x = 75;
          float y = 75;
          float z = 75;
          String id = name;

          Point point = pointMaker.newMessage();
          point.setX(x);
          point.setY(y);
          point.setZ(z);

          Pose pose = poseMaker.newMessage();
          pose.setPosition(point);

          PoseWithCovariance poseCov = poseCovMaker.newMessage();
          poseCov.setPose(pose);

          PoseWithCovarianceStamped poseCovStamped = poseCovStampedMaker.newMessage();
          poseCovStamped.setPose(poseCov);

          ObjectType type = typeMaker.newMessage();
          type.setKey(id);

          RecognizedObject object = objectMaker.newMessage();
          object.setPose(poseCovStamped);
          object.setType(type);
          objects.add(object);
        }

        for (String name : tools) {
          float x = 25;
          float y = 25;
          float z = 25;
          String id = name;

          Point point = pointMaker.newMessage();
          point.setX(x);
          point.setY(y);
          point.setZ(z);

          Pose pose = poseMaker.newMessage();
          pose.setPosition(point);

          PoseWithCovariance poseCov = poseCovMaker.newMessage();
          poseCov.setPose(pose);

          PoseWithCovarianceStamped poseCovStamped = poseCovStampedMaker.newMessage();
          poseCovStamped.setPose(poseCov);

          ObjectType type = typeMaker.newMessage();
          type.setKey(id);

          RecognizedObject object = objectMaker.newMessage();
          object.setPose(poseCovStamped);
          object.setType(type);
          objects.add(object);
        }

        message.setObjects(objects);
        publisher.publish(message);
      }

      @Override
      protected void setup() {
        sequenceNumber = 0;
      }

      @Override
      protected void loop() throws InterruptedException {
        publishBowlMessage();
        sequenceNumber++;
        Thread.sleep(1000);
      }
    });
  }

  public List<String> makeContainerList() {
   List<String> containers = new ArrayList<String>();
    containers.add("baking_dish");

    containers.add("dry_bowl");
    containers.add("wet_bowl");

    containers.add("flour_bowl");
    containers.add("cocoa_bowl");

    containers.add("butter_bowl");
    containers.add("eggs_bowl");

    return containers;
  }

  public List<String> makeToolsList() {
   List<String> tools = new ArrayList<String>();
    tools.add("spatula");
    tools.add("whisk");

    return tools;
  }
}
