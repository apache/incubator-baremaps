/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.raster.gdal;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Options {

  private final List<String> options;

  public Options() {
    this.options = new ArrayList<>();
  }

  public Options add(String value) {
    options.add(value);
    return this;
  }

  public Options add(int value) {
    options.add(String.valueOf(value));
    return this;
  }

  public Options add(double value) {
    options.add(String.valueOf(value));
    return this;
  }

  public Options add(boolean value) {
    options.add(String.valueOf(value));
    return this;
  }

  public Vector<String> asVector() {
    return new Vector<>(options);
  }

}
