package io.gazetteer.osm.model;

import com.google.common.base.Objects;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Info {

  private final long id;
  private final int version;
  private final long timestamp;
  private final long changeset;
  private final int uid;
  private final Map<String, String> tags;

  public Info(
      long id, int version, long timestamp, long changeset, int uid, Map<String, String> tags) {
    checkNotNull(uid);
    checkNotNull(tags);
    this.id = id;
    this.version = version;
    this.timestamp = timestamp;
    this.changeset = changeset;
    this.uid = uid;
    this.tags = tags;
  }

  public long getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public long getChangeset() {
    return changeset;
  }

  public int getUid() {
    return uid;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Info info = (Info) o;
    return id == info.id
        && version == info.version
        && timestamp == info.timestamp
        && changeset == info.changeset
        && Objects.equal(uid, info.uid)
        && Objects.equal(tags, info.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, version, timestamp, changeset, uid, tags);
  }
}
