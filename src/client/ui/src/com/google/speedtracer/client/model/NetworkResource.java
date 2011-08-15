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
package com.google.speedtracer.client.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.coreext.client.JSOArray;
import com.google.speedtracer.client.model.NetworkResponseReceivedEvent.Response;
import com.google.speedtracer.client.model.ResourceUpdateEvent.UpdateResource;
import com.google.speedtracer.client.util.Url;

/**
 * Data Payload for Network Resource Events.
 */
public class NetworkResource {

  /**
   * Header Map of Strings.
   */
  public static class HeaderMap extends JavaScriptObject {
    /**
     * interface that allows for iteration through Object Map.
     */
    public interface IterationCallBack {
      void onIteration(String key, String value);
    }

    protected HeaderMap() {
    }

    public final native String get(String key) /*-{
      return this[key];
    }-*/;

    public final native void iterate(IterationCallBack cb) /*-{
      for (var key in this) {
      cb.@com.google.speedtracer.client.model.NetworkResource.HeaderMap.IterationCallBack::onIteration(Ljava/lang/String;Ljava/lang/String;)(key,this[key]);
      }
    }-*/;

    public final native void put(String key, String value) /*-{
      this[key] = value;
    }-*/;
  }

  private static final String SERVER_TRACE_HEADER_NAME = "X-TraceUrl";

  public static boolean isRedirect(int statusCode) {
    return statusCode == 302 || statusCode == 301;
  }

  private boolean cached;

  private double connectDuration = -1;

  private int connectionID = 0;

  private boolean connectionReused = false;

  private int dataLength = 0;

  private boolean didFail;

  private double dnsDuration = -1;

  private double endTime = Double.NaN;

  // Kept around only because hintlets can be accumulated on it.
  private ResourceFinishEvent finishEvent;

  // This allows us to push detailed timing before it is live in dev channel
  private boolean hasDetailedTiming = false;

  private final String httpMethod;

  private final String identifier;

  private String lastPathComponent;

  private String mimeType;

  private double proxyDuration = -1;

  private HeaderMap requestHeaders;

  private double requestTime = 0;

  // Kept around only because hintlets can be accumulated on it.
  private ResourceResponseEvent responseEvent;

  private HeaderMap responseHeaders;

  private double responseReceivedTime = Double.NaN;

  private double sendDuration = 0;

  private double sslDuration = -1;

  // Kept around only because hintlets can be accumulated on it.
  private final ResourceWillSendEvent startEvent;

  private final double startTime;

  private int statusCode = -1;

  private String statusText = "";

  private final String url;

  public NetworkResource(ResourceWillSendEvent startEvent) {
    this.startTime = startEvent.getTime();
    this.identifier = startEvent.getRequestId();
    this.url = startEvent.getUrl();
    this.httpMethod = startEvent.getHttpMethod();
    // Cache the ResourceEvent to later pull hintlets.
    this.startEvent = startEvent;
  }

  /**
   * Used for testing only. This constructor allows for creating mock network
   * resources. If you use it for anything else, zundel will punish you with the
   * electric plunger.
   */
  protected NetworkResource(double startTime, String identifier, String url,
      String httpMethod, HeaderMap requestHeaders, int status,
      HeaderMap responseHeaders) {
    this.startTime = startTime;
    this.identifier = identifier;
    this.url = url;
    this.httpMethod = httpMethod;
    this.requestHeaders = requestHeaders;
    this.statusCode = status;
    this.responseHeaders = responseHeaders;

    this.startEvent = null;
  }

  public String asString() {
    return getIdentifier() + " , " + getUrl() + " , " + getStartTime() + " , "
        + getResponseReceivedTime() + " , " + getEndTime();
  }

  public boolean didFail() {
    return didFail;
  }

  public String formatHttpStatus() {
    if (statusCode > 0) {
      return statusCode + ((statusText.equals("")) ? "" : " - " + statusText);
    } else {
      return "No Response";
    }
  }

  public double getConnectDuration() {
    return connectDuration;
  }

  public int getConnectionID() {
    return connectionID;
  }

  public boolean getConnectionReused() {
    return connectionReused;
  }

  public int getDataLength() {
    return dataLength;
  }

  public double getDnsDuration() {
    return dnsDuration;
  }

  public double getEndTime() {
    return endTime;
  }

  /**
   * Accumulates and returns any hints that were associated with records for
   * this network resource.
   * 
   * @return JSOArray of HintRecords.
   */
  public JSOArray<HintRecord> getHintRecords() {
    JSOArray<HintRecord> hints = JSOArray.createArray().cast();

    if (startEvent.hasHintRecords()) {
      hints = hints.concat(startEvent.getHintRecords());
    }

    if (responseEvent != null && responseEvent.hasHintRecords()) {
      hints = hints.concat(responseEvent.getHintRecords());
    }

    if (finishEvent != null && finishEvent.hasHintRecords()) {
      hints = hints.concat(finishEvent.getHintRecords());
    }

    if (hints.size() <= 0) {
      return null;
    }

    return hints;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getLastPathComponent() {
    if (lastPathComponent == null) {
      lastPathComponent = computeLastPathComponent();
    }
    return lastPathComponent;
  }

  public String getMimeType() {
    return mimeType;
  }

  public double getOnlyConnectDuration() {
    if (dnsDuration > 0) {
      return connectDuration - dnsDuration;
    } else {
      return connectDuration;
    }
  }

  public double getProxyDuration() {
    return proxyDuration;
  }

  public HeaderMap getRequestHeaders() {
    return requestHeaders;
  }

  public double getRequestTime() {
    return requestTime;
  }

  public HeaderMap getResponseHeaders() {
    return responseHeaders;
  }

  public double getResponseReceivedTime() {
    return responseReceivedTime;
  }

  public double getSendDuration() {
    return sendDuration;
  }

  /**
   * Gets the full URL for the server-side trace for this resource. Callers are
   * required to check {@link #hasServerTraceUrl()} before calling this method.
   * 
   * @see #hasServerTraceUrl()
   * 
   * @return full URL
   */
  public String getServerTraceUrl() {
    assert hasServerTraceUrl() : "hasServerTraceUrl is false for this resource";
    return new Url(getUrl()).getOrigin()
        + responseHeaders.get(SERVER_TRACE_HEADER_NAME);
  }

  public double getSslDuration() {
    return sslDuration;
  }

  public double getStartTime() {
    return startTime;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getStatusText() {
    return statusText;
  }

  public String getUrl() {
    return url;
  }

  /**
   * Indicates whether this network resource has detailed timing information.
   * 
   * @return
   */
  public boolean hasDetailedTiming() {
    return hasDetailedTiming;
  }

  /**
   * Indicates whether this resource has a server-side trace url associated with
   * it.
   * 
   * @see #getServerTraceUrl()
   * 
   * @return
   */
  public boolean hasServerTraceUrl() {
    return (responseHeaders == null) ? false
        : responseHeaders.get(SERVER_TRACE_HEADER_NAME) != null;
  }

  public boolean isCached() {
    return cached;
  }

  public boolean isDidFail() {
    return didFail;
  }

  public boolean isRedirect() {
    return isRedirect(statusCode);
  }

  // Used to artificially close a resource that might not receive an explicit
  // finish event.
  public void setEndTime(double endTime) {
    this.endTime = endTime;
  }

  public void setResponseReceivedTime(double time) {
    this.responseReceivedTime = time;
  }

  public void update(NetworkDataReceivedEvent dataLengthChange) {
    this.dataLength += dataLengthChange.getData().<NetworkDataReceivedEvent.Data> cast().getLengthReceived();
  }

  public void update(NetworkResponseReceivedEvent record) {
    NetworkResponseReceivedEvent.Data data = record.getData().cast();
    Response response = data.getResponse();
    this.updateResponse(response);
  }

  public void update(NetworkRequestWillBeSentEvent requestWillBeSent) {
    this.requestHeaders = requestWillBeSent.getData().<NetworkRequestWillBeSentEvent.Data> cast().getRequest().getHeaders();
  }

  public void update(ResourceFinishEvent finishEvent) {
    this.endTime = finishEvent.getTime();
    this.didFail = finishEvent.didFail();
    // Cache the ResourceEvent to later pull hintlets.
    this.finishEvent = finishEvent;
  }

  public void update(ResourceResponseEvent responseEvent) {
    this.responseReceivedTime = responseEvent.getTime();
    this.mimeType = responseEvent.getMimeType();
    this.statusCode = responseEvent.getStatusCode();
    // Cache the ResourceEvent to later pull hintlets.
    this.responseEvent = responseEvent;
  }

  /**
   * NOTE: This should now be dead code in the live tracing case, and is only
   * kept to support loading old saved dumps.
   * 
   * Updates information about this record. Note that we use the timeline
   * checkpoint records to establish all timing information. We therefore ignore
   * the timing information present in these updates.
   * 
   * @param updateEvent
   */
  public void update(ResourceUpdateEvent updateEvent) {
    UpdateResource update = updateEvent.getUpdate();
    if (update.didRequestChange()) {
      this.requestHeaders = update.getRequestHeaders();
    }

    if (update.didResponseChange()) {
      this.responseHeaders = update.getResponseHeaders();
      this.cached = update.wasCached();
      this.connectionID = update.getConnectionID();
      this.connectionReused = update.getConnectionReused();

      if (this.statusCode < 0) {
        this.statusCode = update.getStatusCode();
        this.statusText = update.getStatusText();
      }

      DetailedResponseTiming detailedTiming = update.getDetailedResponseTiming();
      if (detailedTiming != null) {
        this.hasDetailedTiming = true;
        this.requestTime = detailedTiming.getRequestTime();
        this.proxyDuration = detailedTiming.getProxyDuration();
        this.dnsDuration = detailedTiming.getDnsDuration();
        this.connectDuration = detailedTiming.getConnectDuration();
        this.sendDuration = detailedTiming.getSendDuration();
        this.sslDuration = detailedTiming.getSslDuration();
      }
    }

    if (update.didLengthChange()) {
      this.dataLength = update.getContentLength();
    }

    if (update.didTimingChange()) {
      if ((Double.isNaN(this.endTime)) && (update.getEndTime() > 0)) {
        this.endTime = update.getEndTime();
      }

      if ((Double.isNaN(this.responseReceivedTime))
          && (update.getResponseReceivedTime() > 0)) {
        this.responseReceivedTime = update.getResponseReceivedTime();
      }
    }
  }

  public void updateResponse(NetworkResponseReceivedEvent.Response response) {
    this.responseHeaders = response.getHeaders();
    this.cached = response.wasCached();
    this.connectionID = response.getConnectionID();
    this.connectionReused = response.getConnectionReused();
    this.statusCode = response.getStatus();

    DetailedResponseTiming detailedTiming = response.getDetailedTiming();
    if (detailedTiming != null) {
      this.hasDetailedTiming = true;
      this.requestTime = detailedTiming.getRequestTime();
      this.proxyDuration = detailedTiming.getProxyDuration();
      this.dnsDuration = detailedTiming.getDnsDuration();
      this.connectDuration = detailedTiming.getConnectDuration();
      this.sendDuration = detailedTiming.getSendDuration();
      this.sslDuration = detailedTiming.getSslDuration();
    }
  }

  private String computeLastPathComponent() {
    int lastSlash = url.lastIndexOf('/');
    if (lastSlash < 0) {
      // Might be something like about:blank.
      return url;
    }
    int afterSlash = lastSlash + 1;
    if (afterSlash == url.length()) {
      return "/";
    }
    return url.substring(afterSlash, url.length());
  }
}
