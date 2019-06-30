package io.gazetteer.osm.model;

public class Header {

  private final long replicationTimestamp;
  private final long replicationSequenceNumber;
  private final String replicationUrl;
  private final String bbox;

  public Header(long replicationTimestamp, long replicationSequenceNumber, String replicationUrl, String bbox) {
    this.replicationTimestamp = replicationTimestamp;
    this.replicationSequenceNumber = replicationSequenceNumber;
    this.replicationUrl = replicationUrl;
    this.bbox = bbox;
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

  public String getBbox() {
    return bbox;
  }
}
