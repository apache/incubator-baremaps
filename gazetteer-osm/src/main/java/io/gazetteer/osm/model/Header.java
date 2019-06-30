package io.gazetteer.osm.model;

public class Header {

  private final long replicationTimestamp;
  private final long replicationSequenceNumber;
  private final String replicationUrl;
  private final String source;
  private final String writingProgram;
  private final String bbox;

  public Header(long replicationTimestamp, long replicationSequenceNumber, String replicationUrl, String source, String writingProgram,
      String bbox) {
    this.replicationTimestamp = replicationTimestamp;
    this.replicationSequenceNumber = replicationSequenceNumber;
    this.replicationUrl = replicationUrl;
    this.source = source;
    this.writingProgram = writingProgram;
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

  public String getSource() {
    return source;
  }

  public String getWritingProgram() {
    return writingProgram;
  }

  public String getBbox() {
    return bbox;
  }
}
