package io.gazetteer.osm.model;

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
