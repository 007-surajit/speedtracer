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
package com.google.speedtracer.latencydashboard.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AreaChart;
import com.google.gwt.visualization.client.visualizations.Gauge;
import com.google.gwt.visualization.client.visualizations.LineChart;
import com.google.gwt.visualization.client.visualizations.PieChart;
import com.google.speedtracer.latencydashboard.shared.DashboardRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StartupLatencyDashboard implements EntryPoint {
  private final List<LatencyDashboardChart> charts = new ArrayList<LatencyDashboardChart>();
  private final Button refreshButton = new Button("Refresh");
  private final TimelineServiceAsync timelineService = GWT.create(TimelineService.class);

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    DashboardResources.init();
    WarningPane.init(DashboardResources.getResources());
    createDashboardUi();
  }

  private void createCharts() {
    charts.add(new AggregatedEventTypeChart(DashboardResources.getResources(),
        "Page Load breakdown by Event"));
    charts.add(new GwtLightweightMetricsChart(
        DashboardResources.getResources(),
        "GWT Lightweight Metrics - Page Load"));
    charts.add(new LoadEventChart(DashboardResources.getResources(),
        "Page Load Event times"));

    for (LatencyDashboardChart chart : charts) {
      RootPanel.get().add(chart);
    }
  }

  private void createDashboardUi() {
    refreshButton.getElement().getStyle().setMarginLeft(3, Unit.EM);
    refreshButton.setEnabled(false);
    refreshButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        populateDashboard();
      }
    });

    VisualizationUtils.loadVisualizationApi(new Runnable() {
      /**
       * Load data from the server.
       */
      public void run() {
        refreshButton.setEnabled(true);
        createCharts();
        populateDashboard();
        RootPanel.get().add(refreshButton);
        Window.addResizeHandler(new ResizeHandler() {

          public void onResize(ResizeEvent event) {
            populateDashboard();
          }

        });
      }
    }, AreaChart.PACKAGE, LineChart.PACKAGE, PieChart.PACKAGE, Gauge.PACKAGE);
  }

  private void populateDashboard() {
    timelineService.getDashboardLatestRecords(15,
        new AsyncCallback<DashboardRecord[]>() {
          public void onFailure(Throwable caught) {
            WarningPane.get().show(
                "Couldn't retrieve dashboard data.  Check the server and try to 'Refresh'");
          }

          public void onSuccess(DashboardRecord[] result) {
            if (result.length > 0) {
              for (LatencyDashboardChart chart : charts) {
                chart.populateChart(result);
              }
            } else {
              WarningPane.get().show(
                  "Retrieved empty dashboard data. Check the server and try to 'Refresh'");
            }
          }
        });
  }
}
