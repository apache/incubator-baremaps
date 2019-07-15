package io.gazetteer.osm.osmxml;

public class State {

  public final long sequenceNumber;

  public final long timestamp;

  public State(long sequenceNumber, long timestamp) {
    this.sequenceNumber = sequenceNumber;
    this.timestamp = timestamp;
  }
}
