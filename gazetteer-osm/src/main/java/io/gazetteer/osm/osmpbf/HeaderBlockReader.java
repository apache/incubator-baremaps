package io.gazetteer.osm.osmpbf;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBBox;

public class HeaderBlockReader {

  private final Osmformat.HeaderBlock headerBlock;

  public HeaderBlockReader(Osmformat.HeaderBlock headerBlock) {
    this.headerBlock = headerBlock;
  }

  public HeaderBlock readHeaderBlock() {
    long replicationTimestamp = headerBlock.getOsmosisReplicationTimestamp();
    long replicationSequenceNumber = headerBlock.getOsmosisReplicationSequenceNumber();
    String replicationUrl = headerBlock.getOsmosisReplicationBaseUrl();
    String source = headerBlock.getSource();
    String writingProgram = headerBlock.getWritingprogram();
    HeaderBBox headerBBox = headerBlock.getBbox();
    double x1 = headerBBox.getLeft() * .000000001;
    double x2 = headerBBox.getRight() * .000000001;
    double y1 = headerBBox.getBottom() * .000000001;
    double y2 = headerBBox.getTop() * .000000001;
    GeometryFactory geometryFactory = new GeometryFactory();
    Point p1 = geometryFactory.createPoint(new Coordinate(x1, y1));
    Point p2 = geometryFactory.createPoint(new Coordinate(x2, y2));
    Geometry bbox = geometryFactory.createMultiPoint(new Point[]{p1, p2}).getEnvelope();
    return new HeaderBlock(replicationTimestamp, replicationSequenceNumber, replicationUrl, source, writingProgram, bbox);
  }

}
