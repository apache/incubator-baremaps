package com.baremaps.osm.pbf;

import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.binary.Osmformat.HeaderBBox;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class HeaderBlock implements FileBlock {

  public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  private final Osmformat.HeaderBlock headerBlock;

  public HeaderBlock(Osmformat.HeaderBlock headerBlock) {
    this.headerBlock = headerBlock;
  }

  public Header readHeader() {
    LocalDateTime timestamp = LocalDateTime
        .ofEpochSecond(headerBlock.getOsmosisReplicationTimestamp(), 0, ZoneOffset.UTC);
    return new Header(
        timestamp,
        headerBlock.getOsmosisReplicationSequenceNumber(),
        headerBlock.getOsmosisReplicationBaseUrl(),
        headerBlock.getSource(),
        headerBlock.getWritingprogram());
  }

  public Bound readBounds() {
    HeaderBBox headerBBox = headerBlock.getBbox();
    double minLon = headerBBox.getLeft() * .000000001;
    double maxLon = headerBBox.getRight() * .000000001;
    double minLat = headerBBox.getBottom() * .000000001;
    double maxLat = headerBBox.getTop() * .000000001;
    return new Bound(maxLat, maxLon, minLat, minLon);
  }

  @Override
  public void readEntities(Consumer<Entity> consumer) {
    consumer.accept(readHeader());
    consumer.accept(readBounds());
  }

}
