package com.baremaps.osm.pbf;

import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.binary.Osmformat.HeaderBBox;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Header;
import com.google.protobuf.InvalidProtocolBufferException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.zip.DataFormatException;

public class HeaderBlockReader {

  public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  private final Blob blob;

  private final Osmformat.HeaderBlock headerBlock;

  public HeaderBlockReader(Blob blob) throws DataFormatException, InvalidProtocolBufferException {
    this.blob = blob;
    this.headerBlock = Osmformat.HeaderBlock.parseFrom(blob.data());
  }

  public HeaderBlock readHeaderBlock() {
    LocalDateTime timestamp = LocalDateTime.ofEpochSecond(headerBlock.getOsmosisReplicationTimestamp(), 0, ZoneOffset.UTC);
    Long replicationSequenceNumber = headerBlock.getOsmosisReplicationSequenceNumber();
    String replicationBaseUrl = headerBlock.getOsmosisReplicationBaseUrl();
    String source = headerBlock.getSource();
    String writingProgram = headerBlock.getWritingprogram();
    Header header = new Header(timestamp, replicationSequenceNumber, replicationBaseUrl, source, writingProgram);

    HeaderBBox headerBBox = headerBlock.getBbox();
    double minLon = headerBBox.getLeft() * .000000001;
    double maxLon = headerBBox.getRight() * .000000001;
    double minLat = headerBBox.getBottom() * .000000001;
    double maxLat = headerBBox.getTop() * .000000001;
    Bound bound = new Bound(maxLat, maxLon, minLat, minLon);

    return new HeaderBlock(blob, header, bound);
  }


}
