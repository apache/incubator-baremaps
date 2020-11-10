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

package com.baremaps.osm.domain;

import com.baremaps.osm.EntityHandler;
import java.time.LocalDateTime;

/**
 * A class used to store the metadata of an dataset.
 */
public class Header implements Entity {

  private final LocalDateTime replicationTimestamp;
  private final Long replicationSequenceNumber;
  private final String replicationUrl;
  private final String source;
  private final String writingProgram;

  public Header(LocalDateTime replicationTimestamp, Long replicationSequenceNumber, String replicationUrl,
      String source,
      String writingProgram) {
    this.replicationTimestamp = replicationTimestamp;
    this.replicationSequenceNumber = replicationSequenceNumber;
    this.replicationUrl = replicationUrl;
    this.source = source;
    this.writingProgram = writingProgram;
  }

  public LocalDateTime getReplicationTimestamp() {
    return replicationTimestamp;
  }

  public Long getReplicationSequenceNumber() {
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

  @Override
  public void visit(EntityHandler visitor) throws Exception {
    visitor.handle(this);
  }

}
