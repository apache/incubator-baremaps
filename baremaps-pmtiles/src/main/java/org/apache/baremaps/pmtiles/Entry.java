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

package org.apache.baremaps.pmtiles;

class Entry {
  private long tileId;
  private long offset;
  private long length;
  private long runLength;

  public Entry() {}

  public Entry(long tileId, long offset, long length, long runLength) {
    this.tileId = tileId;
    this.offset = offset;
    this.length = length;
    this.runLength = runLength;
  }

  public long getTileId() {
    return tileId;
  }

  public void setTileId(long tileId) {
    this.tileId = tileId;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

  public long getRunLength() {
    return runLength;
  }

  public void setRunLength(long runLength) {
    this.runLength = runLength;
  }
}
