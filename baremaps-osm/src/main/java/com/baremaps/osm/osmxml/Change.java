package com.baremaps.osm.osmxml;

import com.baremaps.osm.model.Entity;

public final class Change {

  enum Type {delete, create, modify}

  private final Type type;
  private final Entity entity;

  public Change(Type type, Entity entity) {
    this.type = type;
    this.entity = entity;
  }

  public Type getType() {
    return type;
  }

  public Entity getEntity() {
    return entity;
  }
}
