package com.baremaps.osm.reader.pbf;

import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.binary.Osmformat.HeaderBBox;
import com.baremaps.osm.model.Header;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class HeaderBlock implements FileBlock {

  private final Header header;

  public HeaderBlock(Header header) {
    this.header = header;
  }

  public Header getHeader() {
    return header;
  }

  public static class Builder {

    public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final Osmformat.HeaderBlock headerBlock;

    public Builder(Osmformat.HeaderBlock headerBlock) {
      this.headerBlock = headerBlock;
    }

    public HeaderBlock build() {
      HeaderBBox headerBBox = headerBlock.getBbox();
      LocalDateTime timestamp = LocalDateTime
          .ofEpochSecond(headerBlock.getOsmosisReplicationTimestamp(), 0, ZoneOffset.UTC);
      double x1 = headerBBox.getLeft() * .000000001;
      double x2 = headerBBox.getRight() * .000000001;
      double y1 = headerBBox.getBottom() * .000000001;
      double y2 = headerBBox.getTop() * .000000001;
      GeometryFactory geometryFactory = new GeometryFactory();
      Point p1 = geometryFactory.createPoint(new Coordinate(x1, y1));
      Point p2 = geometryFactory.createPoint(new Coordinate(x2, y2));
      return new HeaderBlock(new Header(
          timestamp,
          headerBlock.getOsmosisReplicationSequenceNumber(),
          headerBlock.getOsmosisReplicationBaseUrl(),
          headerBlock.getSource(),
          headerBlock.getWritingprogram(),
          geometryFactory.createMultiPoint(new Point[]{p1, p2}).getEnvelope()
      ));
    }

  }
}
