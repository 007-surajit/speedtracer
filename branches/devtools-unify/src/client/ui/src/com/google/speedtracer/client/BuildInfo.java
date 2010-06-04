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
package com.google.speedtracer.client;

import java.util.Date;

/**
 * Contains information about the current build. To get a concrete instance, use
 * {@link com.google.gwt.core.client.GWT#create(Class)}. A generator will
 * include information supplied during the build process.
 */
public interface BuildInfo {

  /**
   * The source revision this build is based on.
   * 
   * @return the revision
   */
  int getBuildRevision();

  /**
   * The time this was built.
   * 
   * @return
   */
  Date getBuildTime();
}
