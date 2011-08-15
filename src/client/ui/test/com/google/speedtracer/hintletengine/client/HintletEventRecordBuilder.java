/*
 * Copyright 2011 Google Inc.
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
package com.google.speedtracer.hintletengine.client;

import com.google.speedtracer.client.model.DomEvent;
import com.google.speedtracer.client.model.GarbageCollectionEvent;
import com.google.speedtracer.client.model.LayoutEvent;
import com.google.speedtracer.client.model.NetworkDataReceivedEvent;
import com.google.speedtracer.client.model.PaintEvent;
import com.google.speedtracer.client.model.ParseHtmlEvent;
import com.google.speedtracer.client.model.ResourceDataReceivedEvent;
import com.google.speedtracer.client.model.ResourceFinishEvent;
import com.google.speedtracer.client.model.ResourceResponseEvent;
import com.google.speedtracer.client.model.ResourceWillSendEvent;
import com.google.speedtracer.client.model.TabChangeEvent;
import com.google.speedtracer.client.model.UiEvent;

/**
 * Utility class for creating various EventRecord objects for
 * use with test Hintlets.
 * 
 * TODO(knorton): Most of the create methods in this class should be
 * moved into static methods in the type they create in order to
 * propertly encapsulate the data structure.
 */
public class HintletEventRecordBuilder {

  public static final int DEFAULT_TIME = 1;
  public static final int DEFAULT_SEQUENCE = 1;
  public static final String DEFAULT_ID = "1";

  /**
   * This method sets time equal to sequence.
   */  
  public static NetworkDataReceivedEvent createNetworkDataRecieved(String requestId,
      int sequence, int dataLength){
    return createNetworkDataRecieved(requestId, sequence, sequence, dataLength);
  }
  
  public native static NetworkDataReceivedEvent createNetworkDataRecieved(String requestId,
      int time, int sequence, int dataLength)/*-{
    return {
      "type" : @com.google.speedtracer.shared.EventRecordType::NETWORK_DATA_RECEIVED,
      "time" : time,
      "data" : {
        "requestId" : requestId,
        "dataLength" : dataLength
      },
      "sequence" : sequence
    };
  }-*/;
  

  public static NetworkDataReceivedEvent createNetworkDataRecieved(int dataLength){
    return createNetworkDataRecieved(DEFAULT_ID, DEFAULT_TIME, DEFAULT_SEQUENCE, dataLength);
  }

  public native static DomEvent createDomEvent(double duration) /*-{
    var event = @com.google.speedtracer.hintletengine.client.HintletEventRecordBuilder::createUiEvent(ID)
    (@com.google.speedtracer.shared.EventRecordType::DOM_EVENT,duration);
    event.data = {"type" : ""};
    return event;
  }-*/;
  
  public native static GarbageCollectionEvent createGCEvent(double duration) /*-{
    var event = @com.google.speedtracer.hintletengine.client.HintletEventRecordBuilder::createUiEvent(ID)
    (@com.google.speedtracer.shared.EventRecordType::GC_EVENT,duration);
    event.data = {"usedHeapSizeDelta" : 600};
    return event;
  }-*/;

  public native static LayoutEvent createLayoutEvent(double duration) /*-{
    var event = @com.google.speedtracer.hintletengine.client.HintletEventRecordBuilder::createUiEvent(ID)
    (@com.google.speedtracer.shared.EventRecordType::LAYOUT_EVENT,duration);
    return event;
  }-*/;
  
  public native static PaintEvent createPaintEvent(double duration) /*-{
    var event = @com.google.speedtracer.hintletengine.client.HintletEventRecordBuilder::createUiEvent(ID)
    (@com.google.speedtracer.shared.EventRecordType::PAINT_EVENT,duration);
    event.data = {
        "x" : 1,
        "y" : 1,
        "width" : 1,
        "height" : 1
    };
    return event;
  }-*/;
  
  public native static ParseHtmlEvent createParseHtmlEvent(double duration) /*-{
    var event = @com.google.speedtracer.hintletengine.client.HintletEventRecordBuilder::createUiEvent(ID)
    (@com.google.speedtracer.shared.EventRecordType::PARSE_HTML_EVENT,duration);
    event.data = {
      "length" : 1,
      "startLine" : 1,
      "endLine" : 1
    };
    return event;
  }-*/;
  
  private native static UiEvent createUiEvent(int type, double duration) /*-{
    return {
      "type" : type,
      "time" : @com.google.speedtracer.hintletengine.client.HintletEventRecordBuilder::DEFAULT_TIME,
      "data" : {},
      "duration" : duration,
      "children" : [],
      "sequence" : @com.google.speedtracer.hintletengine.client.HintletEventRecordBuilder::DEFAULT_SEQUENCE
    };
  }-*/;

  public static ResourceWillSendEvent createResourceSendRequest(String url) {
    return createResourceSendRequest(url, DEFAULT_TIME, DEFAULT_SEQUENCE, DEFAULT_ID);
  }

  /**
   * This method sets time equal to sequence
   */
  public static ResourceWillSendEvent createResourceSendRequest(String requestId, String url, int sequence){
    return createResourceSendRequest(url, sequence, sequence, requestId);
  }
  
  /**
   * Create a start event with the given values
   */
  public static native ResourceWillSendEvent createResourceSendRequest(
      String url, int time, int sequence, String requestId) /*-{
    return {
        "type" : @com.google.speedtracer.shared.EventRecordType::RESOURCE_SEND_REQUEST,
        "time" : time,
        "sequence" : sequence,
        "data" : {
            "requestId" : requestId,
            "url" : url,
            "requestMethod" : "GET"
        }
    };
  }-*/;

  /**
   * Get a default finish event
   */
  public static ResourceFinishEvent createResourceFinish() {
    return createResourceFinish(DEFAULT_TIME, DEFAULT_SEQUENCE, DEFAULT_ID);
  }

  /**
   * This method sets time equal to sequence
   */
  public static ResourceFinishEvent createResourceFinish(String requestId, int sequence) {
    return createResourceFinish(sequence, sequence, requestId);
  }
  
  /**
   * Create a finish event with the given values.
   */
  public static native ResourceFinishEvent createResourceFinish(
      int time, int sequence, String requestId) /*-{
    return {
        "type" : @com.google.speedtracer.shared.EventRecordType::RESOURCE_FINISH,
        "time" : time,
        "sequence" : sequence,
        "data" : {
            "requestId" : requestId,
            "didFail" : false
        }
    }
  }-*/;

  /**
   * This method sets time equal to sequence
   */  
  public native static ResourceResponseEvent createResourceReceiveResponse(String requestId,
      int sequence, String mimeType)/*-{
    return {
      "data" : {
        "requestId" : requestId,
        "statusCode" : 200,
        "mimeType" : mimeType
      },
      "children" : [],
      "type" : @com.google.speedtracer.shared.EventRecordType::RESOURCE_RECEIVE_RESPONSE,
      "duration" : 0.029052734375,
      "time" : sequence,
      "sequence" : sequence
    };
  }-*/;
  
  /**
   * This method sets time equal to sequence
   */
  public native static ResourceDataReceivedEvent createResourceDataReceived(String requestId,
      int sequence)/*-{
    return {
      "data" : {
        "requestId" : requestId
      },
      "children" : [],
      "type" : @com.google.speedtracer.shared.EventRecordType::RESOURCE_DATA_RECEIVED,
      "duration" : 0.02,
      "time" : sequence,
      "sequence" : sequence
    };
  }-*/;

  /**
   * This method sets time equal to sequence
   */
  public native static TabChangeEvent createTabChanged(String url, int sequence)/*-{
    return {
        "data" : {
            "url" : url
        },
        "type" : @com.google.speedtracer.shared.EventRecordType::TAB_CHANGED,
        "time" : sequence,
        "sequence" : sequence
    };
  }-*/;
}
