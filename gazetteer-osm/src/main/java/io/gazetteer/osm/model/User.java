package io.gazetteer.osm.model;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class User {

  public static final User NO_USER = new User(-1, "");

  private final int id;
  private final String name;

  public User(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return id == user.id && Objects.equal(name, user.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, name);
  }
}
