/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.osm.osmpbf;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.binary.Osmformat.HeaderBBox;

public class HeaderBlock {

  private final long replicationTimestamp;
  private final long replicationSequenceNumber;
  private final String replicationUrl;
  private final String source;
  private final String writingProgram;
  private final Geometry bbox;

  public HeaderBlock(long replicationTimestamp, long replicationSequenceNumber, String replicationUrl, String source,
      String writingProgram, Geometry bbox) {
    this.replicationTimestamp = replicationTimestamp;
    this.replicationSequenceNumber = replicationSequenceNumber;
    this.replicationUrl = replicationUrl;
    this.source = source;
    this.writingProgram = writingProgram;
    this.bbox = bbox;
  }

  public HeaderBlock(Osmformat.HeaderBlock headerBlock) {
    this.replicationTimestamp = headerBlock.getOsmosisReplicationTimestamp();
    this.replicationSequenceNumber = headerBlock.getOsmosisReplicationSequenceNumber();
    this.replicationUrl = headerBlock.getOsmosisReplicationBaseUrl();
    this.source = headerBlock.getSource();
    this.writingProgram = headerBlock.getWritingprogram();
    HeaderBBox headerBBox = headerBlock.getBbox();
    double x1 = headerBBox.getLeft() * .000000001;
    double x2 = headerBBox.getRight() * .000000001;
    double y1 = headerBBox.getBottom() * .000000001;
    double y2 = headerBBox.getTop() * .000000001;
    GeometryFactory geometryFactory = new GeometryFactory();
    Point p1 = geometryFactory.createPoint(new Coordinate(x1, y1));
    Point p2 = geometryFactory.createPoint(new Coordinate(x2, y2));
    this.bbox = geometryFactory.createMultiPoint(new Point[]{p1, p2}).getEnvelope();
  }

  public long getReplicationTimestamp() {
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
