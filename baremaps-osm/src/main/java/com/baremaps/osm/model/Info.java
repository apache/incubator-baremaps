package com.baremaps.osm.model;

import java.time.LocalDateTime;

public class Info {

  protected final int version;

  protected final LocalDateTime timestamp;

  protected final long changeset;

  protected final int uid;

  public Info(int version, LocalDateTime timestamp, long changeset, int uid) {
    this.version = version;
    this.timestamp = timestamp;
    this.changeset = changeset;
    this.uid = uid;
  }

  public int getVersion() {
    return version;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public long getChangeset() {
    return changeset;
  }

  public int getUid() {
    return uid;
  }
}
