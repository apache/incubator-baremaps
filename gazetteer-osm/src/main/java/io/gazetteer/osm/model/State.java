package io.gazetteer.osm.model;

public class State {

  public final long timestamp;

  public final long sequenceNumber;

  public State(long timestamp, long sequenceNumber) {
    this.timestamp = timestamp;
    this.sequenceNumber = sequenceNumber;
  }

}
