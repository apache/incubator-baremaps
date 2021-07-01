package com.baremaps.osm.domain;

import com.baremaps.osm.handler.EntityConsumer;
import com.baremaps.osm.handler.EntityFunction;

/**
 * Represents an entity in an OpenStreetMap dataset.
 * Entities are a basis to model all the objects in OpenStreetMap.
 */
public abstract class Entity {

  /**
   * Visits the entity with the provided entity consumer.
   */
  public abstract void visit(EntityConsumer consumer) throws Exception;

  /**
   * Visits the entity with the provided entity function.
   */
  public abstract <T> T visit(EntityFunction<T> function) throws Exception;

}
