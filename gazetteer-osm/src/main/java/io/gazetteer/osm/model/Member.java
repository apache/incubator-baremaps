package io.gazetteer.osm.model;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Member {

  public enum Type {
    node,
    way,
    relation
  }

  private final long ref;

  private final Type type;

  private final String role;

  public Member(long ref, Type type, String role) {
    checkNotNull(type);
    checkNotNull(role);
    this.ref = ref;
    this.type = type;
    this.role = role;
  }

  public long getRef() {
    return ref;
  }

  public Type getType() {
    return type;
  }

  public String getRole() {
    return role;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Member member = (Member) o;
    return ref == member.ref && type == member.type && Objects.equal(role, member.role);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(ref, type, role);
  }

}
