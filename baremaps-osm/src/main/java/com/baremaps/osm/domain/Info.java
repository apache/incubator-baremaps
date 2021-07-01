package com.baremaps.osm.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents all the metadata associated to an element in an OpenStreetMap dataset.
 */
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Info)) {
      return false;
    }
    Info info = (Info) o;
    return version == info.version &&
        changeset == info.changeset &&
        uid == info.uid &&
        Objects.equals(timestamp, info.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, timestamp, changeset, uid);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Info.class.getSimpleName() + "[", "]")
        .add("version=" + version)
        .add("timestamp=" + timestamp)
        .add("changeset=" + changeset)
        .add("uid=" + uid)
        .toString();
  }
}
