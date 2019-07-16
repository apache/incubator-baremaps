package io.gazetteer.osm.osmxml;

import io.gazetteer.osm.model.Entity;

public final class Change {

  private final String type;
  private final Entity entity;

  public Change(String type, Entity entity) {
    this.type = type;
    this.entity = entity;
  }

  public String getType() {
    return type;
  }

  public Entity getEntity() {
    return entity;
  }
}
