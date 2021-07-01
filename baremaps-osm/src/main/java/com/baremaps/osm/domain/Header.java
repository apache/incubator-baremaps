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

import com.baremaps.osm.handler.EntityConsumer;
import com.baremaps.osm.handler.EntityFunction;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A class used to store the metadata of an dataset.
 */
public class Header extends Entity {

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
  public void visit(EntityConsumer handler) throws Exception {
    handler.match(this);
  }

  @Override
  public <T> T visit(EntityFunction<T> mapper) throws Exception {
    return mapper.match(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Header)) {
      return false;
    }
    Header header = (Header) o;
    return Objects.equals(replicationTimestamp, header.replicationTimestamp) &&
        Objects.equals(replicationSequenceNumber, header.replicationSequenceNumber) &&
        Objects.equals(replicationUrl, header.replicationUrl) &&
        Objects.equals(source, header.source) &&
        Objects.equals(writingProgram, header.writingProgram);
  }

  @Override
  public int hashCode() {
    return Objects.hash(replicationTimestamp, replicationSequenceNumber, replicationUrl, source, writingProgram);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Header.class.getSimpleName() + "[", "]")
        .add("replicationTimestamp=" + replicationTimestamp)
        .add("replicationSequenceNumber=" + replicationSequenceNumber)
        .add("replicationUrl='" + replicationUrl + "'")
        .add("source='" + source + "'")
        .add("writingProgram='" + writingProgram + "'")
        .toString();
  }
}
