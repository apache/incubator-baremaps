/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.osm.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class State {

  public final long sequenceNumber;

  public final long timestamp;

  public State(long sequenceNumber, long timestamp) {
    this.sequenceNumber = sequenceNumber;
    this.timestamp = timestamp;
  }

  public static State parse(String state) throws ParseException {
    Map<String, String> map = new HashMap<>();
    for (String line : state.split("\n")) {
      String[] array = line.split("=");
      if (array.length == 2) {
        map.put(array[0], array[1]);
      }
    }
    long sequenceNumber = Long.parseLong(map.get("sequenceNumber"));
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH\\:mm\\:ss'Z'");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    long timestamp = format.parse(map.get("timestamp")).getTime() / 1000;
    return new State(sequenceNumber, timestamp);
  }

}
