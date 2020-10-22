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

import java.time.LocalDateTime;
import org.locationtech.jts.geom.Geometry;

public class Header {

  private final LocalDateTime replicationTimestamp;
  private final long replicationSequenceNumber;
  private final String replicationUrl;
  private final String source;
  private final String writingProgram;
  private final Geometry bbox;

  public Header(LocalDateTime replicationTimestamp, long replicationSequenceNumber, String replicationUrl,
      String source,
      String writingProgram, Geometry bbox) {
    this.replicationTimestamp = replicationTimestamp;
    this.replicationSequenceNumber = replicationSequenceNumber;
    this.replicationUrl = replicationUrl;
    this.source = source;
    this.writingProgram = writingProgram;
    this.bbox = bbox;
  }

  public LocalDateTime getReplicationTimestamp() {
    return replicationTimestamp;
  }

  public long getReplicationSequenceNumber() {
    return replicationSequenceNumber;
  }

  public String getReplicationUrl() {
    return replicationUrl;
  }

  public String getSource() {
    return source;
  }

  public String getWritingProgram() {
    return writingProgram;
  }

  public Geometry getBbox() {
    return bbox;
  }


}
