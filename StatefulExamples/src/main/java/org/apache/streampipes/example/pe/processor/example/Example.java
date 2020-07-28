/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package org.apache.streampipes.example.pe.processor.example;

import com.google.gson.Gson;
import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;

import org.apache.streampipes.wrapper.runtime.StatefulEventProcessor;
import org.apache.streampipes.container.state.database.StateDB;
import org.slf4j.Logger;

public class Example extends
        StatefulEventProcessor<ExampleParameters> {

  private static Logger LOG;
  private ExampleState state = new ExampleState();
  private String elementId;


  //@StateObject
  //public int counter;

  public Example(){
    super();
    this.elementId = "Example" + (Math.random()*100000);
    //this.counter = 0;
    //this.stateHandler = new StateHandler(this);
  }


  @Override
  public void onInvocation(ExampleParameters parameters,
        SpOutputCollector spOutputCollector, EventProcessorRuntimeContext runtimeContext) throws SpRuntimeException {
        StateDB.openDB(elementId);
  }

  @Override
  public void onEvent(Event event, SpOutputCollector out) throws SpRuntimeException {
    System.out.println(state.counter);
    event.addField("counter", ++state.counter);
    out.collect(event);
    saveState();
  }

  @Override
  public void onDetach() throws SpRuntimeException {
    StateDB.closeDB(elementId);
  }

  @Override
  public String getState(){
    return new Gson().toJson(this.state);
  }

  @Override
  public void setState(String state) throws SpRuntimeException {
    this.state = new Gson().fromJson(state, ExampleState.class);
  }

  private void saveState(){
    StateDB.addState(elementId, getState());
  }
}
