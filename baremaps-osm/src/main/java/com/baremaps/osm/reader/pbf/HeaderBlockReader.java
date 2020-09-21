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
package com.baremaps.osm.reader.pbf;

import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.binary.Osmformat.HeaderBBox;
import com.baremaps.osm.model.Header;
import com.google.protobuf.InvalidProtocolBufferException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class HeaderBlockReader {

  public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  private final Osmformat.HeaderBlock headerBlock;

  public HeaderBlockReader(FileBlock fileBlock) throws InvalidProtocolBufferException {
    this.headerBlock = Osmformat.HeaderBlock.parseFrom(fileBlock.getData());
  }

  public Header getHeader() {
    HeaderBBox headerBBox = headerBlock.getBbox();
    LocalDateTime timestamp = LocalDateTime.ofEpochSecond(headerBlock.getOsmosisReplicationTimestamp(), 0, ZoneOffset.UTC);
    double x1 = headerBBox.getLeft() * .000000001;
    double x2 = headerBBox.getRight() * .000000001;
    double y1 = headerBBox.getBottom() * .000000001;
    double y2 = headerBBox.getTop() * .000000001;
    GeometryFactory geometryFactory = new GeometryFactory();
    Point p1 = geometryFactory.createPoint(new Coordinate(x1, y1));
    Point p2 = geometryFactory.createPoint(new Coordinate(x2, y2));
    return new Header(
        timestamp,
        headerBlock.getOsmosisReplicationSequenceNumber(),
        headerBlock.getOsmosisReplicationBaseUrl(),
        headerBlock.getSource(),
        headerBlock.getWritingprogram(),
        geometryFactory.createMultiPoint(new Point[]{p1, p2}).getEnvelope()
    );
  }

}
