/*
 * Copyright 2008 Google Inc.
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

import com.google.gwt.coreext.client.JsIntegerMap;
import com.google.speedtracer.client.model.DataDispatcher;
import com.google.speedtracer.client.model.EventRecord;
import com.google.speedtracer.client.model.HintRecord;
import com.google.speedtracer.client.model.HintletInterface;
import com.google.speedtracer.client.model.JavaScriptProfile;
import com.google.speedtracer.client.model.JavaScriptProfileModel;
import com.google.speedtracer.client.model.UiEvent;
import com.google.speedtracer.client.model.UiEventDispatcher;
import com.google.speedtracer.client.timeline.GraphModel;
import com.google.speedtracer.client.timeline.HighlightModel;
import com.google.speedtracer.client.timeline.ModelData;

import java.util.List;

/**
 * Underlying model implementation that maintains SluggishnessDetailsView's
 * state.
 */
public class SluggishnessModel implements VisualizationModel,
    UiEventDispatcher.UiEventListener, HintletInterface.HintListener {

  /**
   * Listener that is invoked when an existing event has had a change and the
   * display of this event might need to be updated.
   */
  interface EventRefreshListener {
    void onEventRefresh(UiEvent event);
  }

  /**
   * Listener that is invoked when we have an event that pops in within our
   * current window.
   */
  interface EventWithinWindowListener {
    void onEventWithinWindow(UiEvent event);
  }

  /**
   * Default Scale Max for Y axis.
   */
  public static double defaultSluggishnessYScale = 100;

  private double currentLeft = 0;

  private double currentRight = 0;

  private final DataDispatcher dataDispatcher;

  private EventRefreshListener eventRefreshListener;

  private EventWithinWindowListener eventWithinWindowListener;

  private final GraphModel graphModel;

  private final HighlightModel highlightModel = HighlightModel.create();

  private final UiThreadUtilization sluggishness;

  private final JsIntegerMap<String> typesEncountered = JsIntegerMap.create().cast();

  public SluggishnessModel(DataDispatcher dataDispatcher) {
    this.dataDispatcher = dataDispatcher;

    graphModel = GraphModel.createGraphModel(new ModelData(), "", "ms", "",
        "%", false);

    sluggishness = new UiThreadUtilization(graphModel,
        defaultSluggishnessYScale);

    // Register for relevant events.
    dataDispatcher.getUiEventDispatcher().addUiEventListener(this);
    dataDispatcher.getHintletEngineHost().addHintListener(this);
  }

  public void clearData() {
    getGraphModel().clear();
  }

  public void detachFromData() {
    dataDispatcher.getUiEventDispatcher().removeUiEventListener(this);
  }

  public double getCurrentLeft() {
    return currentLeft;
  }

  public double getCurrentRight() {
    return currentRight;
  }

  public DataDispatcher getDataDispatcher() {
    return dataDispatcher;
  }

  public GraphModel getGraphModel() {
    return graphModel;
  }

  public HighlightModel getHighlightModel() {
    return highlightModel;
  }

  /**
   * Gets the indexes of events within the left and right bounds specified. If
   * the bounds have not changed since the previous run, it returns null,
   * indicating that we need not do any extra work since nothing changed.
   * 
   * An empty int[] returned means we found no indexes in the range.
   * 
   * @param left the timestamp of the left bound.
   * @param right the timestamp of the right bound.
   * @param forceCalculation to increase performance, the calculation is not
   *          performed if the bounds have not changed and this parameter is
   *          <code>false</false>.  If <code>true</code>, the calculation is
   *          performed regardless.
   * @return the indexes of events in the range, or null if the bounds have not
   *         changed.
   */
  public int[] getIndexesOfEventsInRange(double left, double right,
      boolean forceCalculation) {
    // if the bounds have not changed we can return.
    if (!forceCalculation && currentLeft == left && currentRight == right) {
      return null;
    }

    currentLeft = left;
    currentRight = right;

    List<UiEvent> eventList = dataDispatcher.getUiEventDispatcher().getEventList();
    int endIndex = EventRecord.getIndexOfRecord(eventList, right);

    // if we get back a negative number, then nothing starts left of
    // right bound.
    if (endIndex < 0 || eventList.size() == 0) {
      return new int[0];
    }

    int eventIndex = endIndex;

    // We are to the right of all data.
    if (endIndex >= eventList.size()) {
      endIndex = eventList.size();
      eventIndex -= 1;
    }

    // TODO (jaimeyap): We just need the start and end indices.
    // Therefore we should eventually just do a binary search for start index.
    // Will need to do based on endTime, and therefore would need a list sorted
    // by end time. Also need a new Comparator object.
    UiEvent e = eventList.get(eventIndex);
    double endTime = e.getEndTime();
    while (endTime > left) {
      // Walk backwards
      --eventIndex;
      if (eventIndex < 0) {
        break;
      } else {
        e = eventList.get(eventIndex);
        endTime = e.getEndTime();
      }
    }

    int[] result = {(eventIndex + 1), endIndex};
    return result;
  }

  public JavaScriptProfile getJavaScriptProfileForEvent(UiEvent event) {
    JavaScriptProfileModel profileModel = getDataDispatcher().getJavaScriptProfileModel();
    return profileModel.getProfileForEvent(event.getSequence());
  }

  /**
   * Returns a text representation of the JavaScript Profile data for this
   * event.
   * 
   * @param event an event to find associated profile data for
   * @param profileType {@link JavaScriptProfileModel} PROFILE_TYPE_XXX
   *          definition.
   * @return a text representation of the profile intended for debugging
   */
  public String getProfileHtmlForEvent(UiEvent event, int profileType) {
    JavaScriptProfileModel profileModel = getDataDispatcher().getJavaScriptProfileModel();
    return profileModel.getProfileHtmlForEvent(event.getSequence(), profileType);
  }

  /**
   * Map of all event types seen. The key is the event type number and the value
   * is the string representation.
   */
  public JsIntegerMap<String> getTypesEncountered() {
    return typesEncountered;
  }

  public void onHint(HintRecord hintlet) {
    // Only process hintlet references to a Ui Event
    int refRecord = hintlet.getRefRecord();
    EventRecord rec = getDataDispatcher().findEventRecordFromSequence(refRecord);
    if (!UiEvent.isUiEvent(rec)) {
      return;
    }
    int value = HighlightModel.severityToHighlight(hintlet);
    highlightModel.addData(rec.getTime(), value);

    double recTime = rec.getTime();
    // See if any record in the current range has been invalidated, if so
    // notify any listeners wanting to hear about such changes.
    if (recTime > getCurrentLeft() && recTime < getCurrentRight()) {
      fireEventRefresh((UiEvent) rec);
    }
  }

  public void onUiEventFinished(UiEvent event) {
    // Compute and add sluggishness points to graph
    sluggishness.enterBlocking(event.getTime());
    sluggishness.releaseBlocking(event.getEndTime());

    // Keep track of all types seen in this model for the filtering feature
    // in the Sluggishness view.
    int eventType = event.getType();
    if (typesEncountered.get(eventType) == null) {
      typesEncountered.put(eventType, EventRecord.typeToString(eventType));
    }

    maybeFireEventWithinWindow(event);
  }

  public void setCurrentLeft(double currentLeft) {
    this.currentLeft = currentLeft;
  }

  public void setCurrentRight(double currentRight) {
    this.currentRight = currentRight;
  }

  public void setEventWithinWindowListener(
      EventWithinWindowListener eventWithinWindowListener) {
    this.eventWithinWindowListener = eventWithinWindowListener;
  }

  public void setRecordRefreshListener(EventRefreshListener listener) {
    eventRefreshListener = listener;
  }

  private void fireEventRefresh(UiEvent event) {
    if (eventRefreshListener != null) {
      eventRefreshListener.onEventRefresh(event);
    }
  }

  private void maybeFireEventWithinWindow(UiEvent event) {
    if (eventWithinWindowListener == null) {
      return;
    }

    double dispatchTime = event.getTime();
    double endTime = event.getEndTime();

    // For initial startup, we want to avoid doing binary searches for within
    // the window bounds. so we do a quick bounds check and append to the
    // window
    // if we need to.
    if (dispatchTime < currentRight && endTime > currentLeft) {
      eventWithinWindowListener.onEventWithinWindow(event);
    }
  }
}
