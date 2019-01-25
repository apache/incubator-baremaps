package io.gazetteer.osm.domain;

public final class Change {

  public enum Type {
    create,
    modify,
    delete
  }

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
