/*
 * Copyright 2010 Google Inc.
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

package com.google.speedtracer.shared;

/**
 * Primitive integer values for the Types of EventRecords to let us 
 * switch on an int field which faster than an if ladder or string hash.
 */

public class EventRecordType {
  // Webkit Timeline Types
  public static final int DOM_EVENT = 0;
  public static final int LAYOUT_EVENT = 1;
  public static final int RECALC_STYLE_EVENT = 2;
  public static final int PAINT_EVENT = 3;
  public static final int PARSE_HTML_EVENT = 4;
  public static final int TIMER_INSTALLED = 5;
  public static final int TIMER_CLEARED = 6;
  public static final int TIMER_FIRED = 7;
  public static final int XHR_READY_STATE_CHANGE = 8;
  public static final int XHR_LOAD = 9;
  public static final int EVAL_SCRIPT_EVENT = 10;
  public static final int LOG_MESSAGE_EVENT = 11;
  public static final int RESOURCE_SEND_REQUEST = 12;
  public static final int RESOURCE_RECEIVE_RESPONSE = 13;
  public static final int RESOURCE_FINISH = 14;
  public static final int JAVASCRIPT_EXECUTION = 15;
  public static final int RESOURCE_DATA_RECEIVED = 16;
  public static final int GC_EVENT = 17;
  public static final int DOM_CONTENT_LOADED = 18;
  public static final int LOAD_EVENT = 19;
  public static final int SCHEDULE_RESOURCE_REQUEST = 20;
  public static final int PROGRAM_EVENT = 21;

  // Speed Tracer Types
  public static final int AGGREGATED_EVENTS = 0x7FFFFFFF;
  public static final int TAB_CHANGED = 0x7FFFFFFE;
  public static final int RESOURCE_UPDATED = 0x7FFFFFFD;
  public static final int PROFILE_DATA = 0x7FFFFFFC;
  public static final int SERVER_EVENT = 0x7FFFFFFB;
  public static final int NETWORK_REQUEST_WILL_BE_SENT = 0x7FFFFFFA;
  public static final int NETWORK_RESPONSE_RECEIVED = 0x7FFFFFF9;
  public static final int NETWORK_DATA_RECEIVED = 0x7FFFFFF8;
  public static final int NETWORK_LOADING_FINISHED = 0x7FFFFFF7;  

  private static final String[] webkitTypeStrings = {
    "Dom Event",                        // 0 DOM_EVENT
    "Layout",                           // 1 LAYOUT_EVENT
    "Style Recalculation",              // 2 RECALC_STYLE_EVENT
    "Paint",                            // 3 PAINT_EVENT
    "Parse HTML",                       // 4 PARSE_HTML_EVENT
    "Timer Installed",                  // 5 TIMER_INSTALLED
    "Timer Cleared",                    // 6 TIMER_CLEARED
    "Timer Fire",                       // 7 TIMER_FIRED
    "XMLHttpRequest",                   // 8 XHR_READY_STATE_CHANGE
    "XHR Load",                         // 9 XHR_LOAD
    "Script Evaluation",                // 10 EVAL_SCRIPT_EVENT
    "Log Message",                      // 11 LOG_MESSAGE_EVENT
    "Resource Request",                 // 12 RESOURCE_SEND_REQUEST
    "Resource Response",                // 13 RESOURCE_RECEIVE_RESPONSE
    "Resource Finish",                  // 14 RESOURCE_FINISH
    "JavaScript Callback",              // 15 JAVASCRIPT_EXECUTION
    "Resource Data Received",           // 16 RESOURCE_DATA_RECEIVED
    "Garbage Collection",               // 17 GC_EVENT
    "Document Parsing Complete",        // 18 DOM_CONTENT_LOADED
    "Window Load Event",                // 19 LOAD_EVENT
    "Schedule Resource Request",        // 20 SCHEDULE_RESOURCE_REQUEST
    "Program",                          // 21 PROGRAM_EVENT
  };

  private static final String[] speedTracerTypeStrings = {
    "AGGREGATED Events",                // 0x7FFFFFFF AGGREGATED_EVENTS
    "Tab Changed",                      // 0x7FFFFFFE TAB_CHANGED
    "Resource Updated",                 // 0x7FFFFFFD RESOURCE_UPDATED
    "JavaScript CPU profile data",      // 0x7FFFFFFC PROFILE_DATA
    "An event from a server-side trace.",   // 0x7FFFFFFB SERVER_EVENT
    "Will Send Request",                // 0x7FFFFFFA NETWORK_REQUEST_WILL_BE_SENT
    "Did receive response",             // 0x7FFFFFF9 NETWORK_RESPONSE_RECEIVED
    "Content Length Changed",           // 0x7FFFFFF8 NETWORK_DATA_RECEIVED
    "Finished Request",                 // 0x7FFFFFF7 NETWORK_LOADING_FINISHED  
  };

  private static final String[] webkitHelpStrings = {
    // 0 DOM_EVENT
    "A top level DOM event fired, such as mousemove or DOMContentLoaded fired.",
    // 1 LAYOUT_EVENT
    "The browser's rendering engine performed layout calculations.",
    // 2 RECALC_STYLE_EVENT
    "The renderer recalculated CSS styles.",
    // 3 PAINT_EVENT
    "The browser's rendering engine updated the screen.",
    // 4 PARSE_HTML_EVENT
    "A block of HTML was parsed.",
    // 5 TIMER_INSTALLED
    "A new JavaScript timer was created.",
    // 6 TIMER_CLEARED
    "A JavaScript timer was cancelled.",
    // 7 TIMER_FIRED
    "A block of JavaScript was executed due to a JavaScript timer firing.",
    // 8 XHR_READY_STATE_CHANGE
    "The handler for an XMLHttpRequest ran.  Check the state field to see if this is an intermediate state or the last call for the request.",
    // 9 XHR_LOAD
    "The onload handler for an XMLHttpRequest ran.",
    // 10 EVAL_SCRIPT_EVENT
    "A block of JavaScript was parsed/compiled and executed. This only includes script encountered via an HTML <script> tag.",
    // 11 LOG_MESSAGE_EVENT
    "A log message written using console.timeStamp.",
    // 12 RESOURCE_SEND_REQUEST
    "A network request was queued up to send.",
    // 13 RESOURCE_RECEIVE_RESPONSE
    "A network resource load began to recieve data from the server.",
    // 14 RESOURCE_FINISH
    "A new request for a network resource completed.",
    // 15 JAVASCRIPT_EXECUTION
    "JavaScript was run in an event dispatch.",
    // 16 RESOURCE_DATA_RECEIVED
    "Processing a file received by the resource loader.",
    // 17 GC_EVENT
    "The JavaScript engine ran its garbage collector to reclaim memory.",
    // 18 DOM_CONTENT_LOADED
    "DomContentLoaded event returned from JavaScript, Styles matched, chrome extension content scripts ran, and the HTML parser completed. Ready to layout and do first paint.",
    // 19 LOAD_EVENT
    "All static resources (like images and CSS) have loaded.",
    // 20 SCHEDULE_RESOURCE_REQUEST
    "A resource request was scheduled to be added to the network queue.",
    // 21 PROGRAM_EVENT
    "Your web application's UI thread is running.",
  };

  private static final String[] speedTracerHelpStrings = {
    // 0x7FFFFFFF AGGREGATED_EVENTS
    "This event represents many short events that have been aggregated to help reduce the total amount of data displayed.",
    // 0x7FFFFFFE TAB_CHANGED
    "Something about the Tab where the page viewed changed.  Usually this is the title string or the location of the page.",
    // 0x7FFFFFFD RESOURCE_UPDATED
    "Details about a Network Resource were updated.",
    // 0x7FFFFFFC PROFILE_DATA
    "Contains raw data from the JavaScript engine profiler.",
    // 0x7FFFFFFB SERVER_EVENT
    "This happened on the server.",
    // 0x7FFFFFFA NETWORK_REQUEST_WILL_BE_SENT
    "Network event indicating a request for a resource is about to go out.",
    // 0x7FFFFFF9 NETWORK_RESPONSE_RECEIVED
    "Network event indicating that we received a response from the server for a resource.",
    // 0x7FFFFFF8 NETWORK_DATA_RECEIVED
    "Network event indicating that the resource loader adjusted the known size of the resource contents.",
    // 0x7FFFFFF7 NETWORK_LOADING_FINISHED
    "Network event indicating that the resource finished loading.",  
  };  

  public static String typeToHelpString(int type) {
    if (type < 0 || type >= webkitHelpStrings.length) {
      // Normalize to speed tracer range types.
      int speedTracerType = Integer.MAX_VALUE - type;
      if (speedTracerType < 0 || speedTracerType >= speedTracerHelpStrings.length) {
        return "(Unknown Event Type: " + type + ")";
      }
      
      return speedTracerHelpStrings[speedTracerType];
    }
    return webkitHelpStrings[type];
  }

  public static String typeToString(int type) {
    if (type < 0 || type >= webkitTypeStrings.length) {
      // Normalize to speed tracer range types.
      int speedTracerType = Integer.MAX_VALUE - type;
      if (speedTracerType < 0 || speedTracerType >= speedTracerTypeStrings.length) {
        return "(Unknown Event Type: " + type + ")";
      }
      
      return speedTracerTypeStrings[speedTracerType];
    }
    return webkitTypeStrings[type];
  }
}
