/*
 * Copyright 2009 Google Inc.
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

import com.google.speedtracer.client.model.HintRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class responsible for sorting hintlet rules.
 */
public class HintletReportModel {

  private List<HintRecord> hints = new ArrayList<HintRecord>();

  public HintletReportModel() {
  }

  /**
   * Returns a map of hints organized by rule name. There is no guarantee for
   * the order of the hint records within the list.
   * 
   * @return a map of hints organized by rule name.
   */
  public Map<String, List<HintRecord>> getHintsByRule() {
    Map<String, List<HintRecord>> result = new TreeMap<String, List<HintRecord>>();
    for (int i = 0, j = hints.size(); i < j; ++i) {
      HintRecord rec = hints.get(i);
      List<HintRecord> list = result.get(rec.getHintletRule());
      if (list == null) {
        list = new ArrayList<HintRecord>();
        result.put(rec.getHintletRule(), list);
      }
      list.add(rec);
    }
    return result;
  }

  /**
   * Returns a map of hints organized by severity name. There is no guarantee
   * for the order of the hint records within the list.
   * 
   * @return
   */
  public Map<Integer, List<HintRecord>> getHintsBySeverity() {
    Map<Integer, List<HintRecord>> result = new TreeMap<Integer, List<HintRecord>>();
    for (int i = 0, j = hints.size(); i < j; ++i) {
      HintRecord rec = hints.get(i);
      List<HintRecord> list = result.get(Integer.valueOf(rec.getSeverity()));
      if (list == null) {
        list = new ArrayList<HintRecord>();
        result.put(Integer.valueOf(rec.getSeverity()), list);
      }
      list.add(rec);
    }
    return result;
  }

  /**
   * This returns a list of all hints sorted by time.
   * 
   * @return a list of all hints sorted by time.
   */
  public List<HintRecord> getHintsByTime() {
    // This will affect the order of the list of hints stored in
    // this.hints.
    sortByTime(hints, false);
    return hints;
  }

  public void setHints(List<HintRecord> hints) {
    this.hints = hints;
  }

  public void sortByDescription(List<HintRecord> hintList,
      final boolean isAscending) {
    Comparator<HintRecord> comparator = new Comparator<HintRecord>() {
      public int compare(HintRecord o1, HintRecord o2) {
        if (isAscending) {
          return o2.getDescription().compareTo(o1.getDescription());
        } else {
          return o1.getDescription().compareTo(o2.getDescription());
        }
      }
    };
    Collections.sort(hintList, comparator);
  }

  public void sortByRuleName(List<HintRecord> hintList,
      final boolean isAscending) {
    Comparator<HintRecord> comparator = new Comparator<HintRecord>() {
      public int compare(HintRecord o1, HintRecord o2) {
        if (isAscending) {
          return o2.getHintletRule().compareTo(o1.getHintletRule());
        } else {
          return o1.getHintletRule().compareTo(o2.getHintletRule());
        }
      }
    };
    Collections.sort(hintList, comparator);
  }

  public void sortBySeverity(List<HintRecord> hintList,
      final boolean isAscending) {
    Comparator<HintRecord> comparator = new Comparator<HintRecord>() {
      public int compare(HintRecord o1, HintRecord o2) {
        int s1;
        int s2;
        if (isAscending) {
          s1 = o2.getSeverity();
          s2 = o1.getSeverity();
        } else {
          s1 = o1.getSeverity();
          s2 = o2.getSeverity();
        }
        if (s1 > s2) {
          return 1;
        } else if (s1 < s2) {
          return -1;
        }
        return 0;
      }
    };
    Collections.sort(hintList, comparator);
  }

  public void sortByTime(List<HintRecord> hintList, final boolean isAscending) {
    Comparator<HintRecord> comparator = new Comparator<HintRecord>() {
      public int compare(HintRecord o1, HintRecord o2) {
        double t1;
        double t2;
        if (isAscending) {
          t1 = o2.getTimestamp();
          t2 = o1.getTimestamp();
        } else {
          t1 = o1.getTimestamp();
          t2 = o2.getTimestamp();
        }
        if (t1 > t2) {
          return 1;
        } else if (t1 < t2) {
          return -1;
        }
        return 0;
      }
    };
    Collections.sort(hintList, comparator);
  }
}
