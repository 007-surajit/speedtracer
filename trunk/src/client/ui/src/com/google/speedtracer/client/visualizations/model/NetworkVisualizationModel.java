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
package com.google.speedtracer.client.visualizations.model;

import com.google.speedtracer.client.model.DataDispatcher;
import com.google.speedtracer.client.model.EventRecord;
import com.google.speedtracer.client.model.HintRecord;
import com.google.speedtracer.client.model.HintletInterface;
import com.google.speedtracer.client.model.NetworkResource;
import com.google.speedtracer.client.model.NetworkEventDispatcher;
import com.google.speedtracer.client.model.ResourceRecord;
import com.google.speedtracer.client.timeline.GraphModel;
import com.google.speedtracer.client.timeline.HighlightModel;
import com.google.speedtracer.client.timeline.ModelData;

import java.util.ArrayList;
import java.util.List;

/**
 * Underlying model implementation that maintains NetworkTimeLineDetailView's
 * state.
 */
public class NetworkVisualizationModel
    implements VisualizationModel, NetworkEventDispatcher.Listener, HintletInterface.HintListener {

  /**
   * Invoked when a resource has a change and may need to be refreshed in the
   * user interface.
   */
  interface ResourceRefreshListener {
    void onResourceRefresh(NetworkResource resource);
  }

  private final DataDispatcher dataDispatcher;

  private final GraphModel graphModel;

  private final HighlightModel highlightModel = HighlightModel.create();

  private int openRequests = 0;

  private List<ResourceRefreshListener> resourceRefreshListeners =
      new ArrayList<ResourceRefreshListener>();

  /**
   * We keep an index sorted by start time.
   */
  private final List<NetworkResource> sortedResources = new ArrayList<NetworkResource>();

  private final NetworkEventDispatcher sourceDispatcher;

  public NetworkVisualizationModel(DataDispatcher dataDispatcher) {
    this.dataDispatcher = dataDispatcher;
    graphModel = GraphModel.createGraphModel(new ModelData(), "", "ms", "", " requests", true);

    // Register for source events
    this.sourceDispatcher = dataDispatcher.getNetworkEventDispatcher();
    sourceDispatcher.addListener(this);
    dataDispatcher.getHintletEngineHost().addHintListener(this);
  }

  public void addResourceRefreshListener(ResourceRefreshListener listener) {
    resourceRefreshListeners.add(listener);
  }

  public void clearData() {
    sortedResources.clear();
  }

  public void detachFromData() {
    sourceDispatcher.removeListener(this);
  }

  public GraphModel getGraphModel() {
    return graphModel;
  }

  public HighlightModel getHighlightModel() {
    return highlightModel;
  }

  /**
   * Gets a stored resource from our book keeping, or null if it hasnt been
   * stored before.
   *
   * @param id the request id of our {@link NetworkResource}
   * @return returns the {@link NetworkResource}
   */
  public NetworkResource getResource(String id) {
    return sourceDispatcher.getResource(id);
  }

  public List<NetworkResource> getSortedResources() {
    return sortedResources;
  }

  public void onHint(HintRecord hintlet) {
    // Only process hintlet references to a Resource Event
    int refRecord = hintlet.getRefRecord();
    EventRecord rec = dataDispatcher.findEventRecordFromSequence(refRecord);
    if (!ResourceRecord.isResourceRecord(rec)) {
      return;
    }
    int value;
    value = HighlightModel.severityToHighlight(hintlet);
    highlightModel.addData(rec.getTime(), value);

    // Notify any listeners wanting to hear about such changes.
    ResourceRecord resourceRecord = rec.<ResourceRecord>cast();
    NetworkResource res = findResourceForRecord(resourceRecord);
    if (res != null) {
      fireResourceRefreshListeners(res);
    }
  }

  public void onNetworkResourceRequestStarted(NetworkResource resource, boolean isRedirect) {
    assert (resource != null) : "Resource null in start!";
    // We don't get a close for a redirect, so we just need to not increment
    // again.
    if (!isRedirect) {
      openRequests++;
    }
    getGraphModel().addData(resource.getStartTime(), openRequests);

    sortedResources.add(resource);
    fireResourceRefreshListeners(resource);
  }

  public void onNetworkResourceResponseFinished(NetworkResource resource) {
    assert (resource != null) : "Resource null in finish!";
    openRequests--;
    getGraphModel().addData(resource.getEndTime(), openRequests);
    fireResourceRefreshListeners(resource);
  }

  public void onNetworkResourceResponseStarted(NetworkResource resource) {
    assert (resource != null) : "Resource null in response!";
    fireResourceRefreshListeners(resource);
  }

  public void onNetworkResourceUpdated(NetworkResource resource) {
    // TODO(jaimeyap): We should check for the load event and the domcontent
    // event here and do something with it.
    assert (resource != null) : "Resource null in update!";
    fireResourceRefreshListeners(resource);
  }

  // TODO(conroy): optimize this since it gets called on every model for every
  // hint
  private NetworkResource findResourceForRecord(ResourceRecord rec) {
    // A simple linear scan will suffice - we don't expect this list to get very
    // big.
    for (int i = 0, l = sortedResources.size(); i < l; ++i) {
      NetworkResource res = sortedResources.get(i);
      if (res.getStartTime() > rec.getTime()) {
        // The rest of the resources in this list started too late.
        return null;
      }
      // TODO (jaimeyap): There is a bug here. We need to match both identifier
      // and Url. We need to bubble up the URL along with the hint for this
      // resource in order to match it.
      if (res.getIdentifier() == rec.getRequestId()) {
        return res;
      }
    }
    return null;
  }

  private void fireResourceRefreshListeners(NetworkResource res) {
    for (int i = 0, l = resourceRefreshListeners.size(); i < l; ++i) {
      resourceRefreshListeners.get(i).onResourceRefresh(res);
    }
  }
}
