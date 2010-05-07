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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;

/**
 * A legend written with an HTML table to sub in for the wierd formatting of the
 * gviz one.
 */
public class Legend extends Composite {
  protected static final String legendFontSize = "14px";
  int currentRow = 0;
  int currentCol = 0;
  Grid legendTable = new Grid(1, 4);

  public Legend() {
    legendTable.getElement().getStyle().setProperty("fontSize", legendFontSize);
    initWidget(legendTable);
  }

  public void addItem(String color, String label) {
    if (currentCol == 0) {
      legendTable.insertRow(currentRow);
    }
    legendTable.setHTML(currentRow, currentCol * 2, getColorSquare(color));
    legendTable.setText(currentRow, (currentCol * 2) + 1, label);
    currentCol++;
    if (currentCol > 1) {
      currentCol = 0;
      currentRow++;
    }
  }

  public void clear() {
    currentRow = 0;
    currentCol = 0;
    this.legendTable.clear(true);
    this.legendTable.resize(1, 4);
  }

  protected String getColorSquare(String colorSpec) {
    return "<div style=\"width: " + legendFontSize + "; height: "
        + legendFontSize + "; background-color: " + colorSpec + ";\"></div>";
  }
}
