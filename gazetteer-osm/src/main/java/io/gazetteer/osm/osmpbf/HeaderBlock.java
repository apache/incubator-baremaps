package io.gazetteer.osm.osmpbf;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import io.gazetteer.osm.binary.Osmformat;
import io.gazetteer.osm.binary.Osmformat.HeaderBBox;

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
