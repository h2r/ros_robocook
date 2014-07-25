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

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;


import object_recognition_msgs.*;
import object_recognition_msgs.GetObjectInformation.*;

import edu.brown.cs.h2r.baking.*;
import burlap.oomdp.core.*;

import java.util.*;


/**
 * A simple {@link Subscriber} {@link NodeMain}.
 */
public class Pubsub extends AbstractNodeMain {
  private boolean initialized;

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("rosjava/listener");
  }

  @Override
  public void onStart(ConnectedNode connectedNode) {
     final Log log = connectedNode.getLog();
     final KitchenDomain kitchen = new KitchenDomain();
     //kitchen.setDebug(true);
     this.setInitializedFalse();
     Subscriber<RecognizedObjectArray> subscriber = connectedNode.newSubscriber("/chatter", RecognizedObjectArray._TYPE);


    subscriber.addMessageListener(new MessageListener<RecognizedObjectArray>() {
      @Override
      public void onNewMessage(RecognizedObjectArray array) {
       /*// Maps object ID to real object name
       final AbstractMap<String, String> object_map = new HashMap<String, String>();

       // maps objectInstance name (object0, object1...) to object ID.
       final AbstractMap<String, String> objectInstance_map = new HashMap<String, String>();

       // Iterate once to see how many disctinct objects ORK has recognized.
       for (final RecognizedObject obj : array.getObjects()) {
          object_map.put(obj.getType().getKey(), null);
       }

       if (object_map.size() == 0) {
          System.out.println("ORK recognized no objects, planning not possible!");
          return;
       }*/
       if (!Pubsub.this.getInitialized()) {
         System.out.println("\nINITIALIZING STATE");
         for (final RecognizedObject obj : array.getObjects()) {
          final String id = obj.getType().getKey();
          final String name = id;
          double x = obj.getPose().getPose().getPose().getPosition().getX();
          double y = obj.getPose().getPose().getPose().getPosition().getY();
          double z = obj.getPose().getPose().getPose().getPosition().getZ();
          // add object
          kitchen.addObject(name, x, y, z);
         }
         State state = kitchen.getCurrentState();

         Pubsub.this.setInitialized();

         System.out.println(state.toString());
         kitchen.plan();
      }
     }

    });
  }

  public boolean getInitialized() {
    return this.initialized;
  }

  public void setInitialized() {
    this.initialized = true;
  }

  public void setInitializedFalse() {
    this.initialized = false;
  }
}
