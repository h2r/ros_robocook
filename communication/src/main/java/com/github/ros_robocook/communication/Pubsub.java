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
import org.ros.internal.message.DefaultMessageFactory;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.service.ServiceClient;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;


import object_recognition_msgs.*;
import object_recognition_msgs.GetObjectInformation.*;

import geometry_msgs.Vector3;
import geometry_msgs.Point;

import edu.brown.cs.h2r.baking.KitchenDomain;
import burlap.oomdp.core.*;

import move_msgs.MoveAction;
import move_msgs.MoveRegion;
import move_msgs.MoveObject;
import move_msgs.BaxterAction;

import java.util.*;


/**
 * A simple {@link Subscriber} {@link NodeMain}.
 */
public class Pubsub extends AbstractNodeMain {
  private boolean initialized;
  private State currentState;
  MessageFactory messageFactory;
  Publisher<BaxterAction> actionPublisher;

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("rosjava/listener");
  }

  @Override
  public void onStart(ConnectedNode connectedNode) {
     final Log log = connectedNode.getLog();
     final KitchenDomain kitchen = new KitchenDomain();
     this.messageFactory = connectedNode.getTopicMessageFactory();
     //kitchen.setDebug(true);
     this.setInitializedFalse();
     this.actionPublisher = connectedNode.newPublisher("/baxter_action", BaxterAction._TYPE);
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
         if (!state.equals(Pubsub.this.currentState))
         {
            Pubsub.this.currentState = state;
            kitchen.plan();
            String[] actionParams = kitchen.getRobotActionParams();
            BaxterAction actionMsg = Pubsub.this.getRobotAction(actionParams);
            Pubsub.this.actionPublisher.publish(actionMsg);
         }

         Pubsub.this.setInitialized();

         System.out.println(state.toString());
      }
     }

    });
  }

  public BaxterAction getRobotAction(String[] actionParams)
  {
    String action = actionParams[0];
    BaxterAction actionMsg = this.messageFactory.newFromType(BaxterAction._TYPE);
    if (action == "move")
    {
      actionMsg.setType(BaxterAction.MOVE);
      MoveAction moveMsg = this.messageFactory.newFromType(MoveAction._TYPE);

      MoveRegion regionMsg = this.getMoveRegion(actionParams[2]);
      MoveObject objectMsg = this.messageFactory.newFromType(MoveObject._TYPE);
      objectMsg.setName(actionParams[1]);
      moveMsg.setObject(objectMsg);
      moveMsg.setRegion(regionMsg);
      actionMsg.setMoveAction(moveMsg);
    }

    return actionMsg;
  }

  public MoveRegion getMoveRegion(String regionName)
  {
    MoveRegion region = this.messageFactory.newFromType(MoveRegion._TYPE);
    region.setName(regionName);
    region.setShape(MoveRegion.SHAPE_CIRCLE);
    
    Point origin = this.messageFactory.newFromType(Point._TYPE);
    origin.setX(1.0);
    origin.setY(1.0);
    origin.setZ(0.0);
    region.setOrigin(origin);


    Vector3 scale = this.messageFactory.newFromType(Vector3._TYPE);
    scale.setX(0.333);
    scale.setY(0.333);
    scale.setZ(0.2);  
    region.setScale(scale);

    return region;
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
