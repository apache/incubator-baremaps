package com.baremaps.osm.domain;

import com.baremaps.osm.handler.EntityConsumer;
import com.baremaps.osm.handler.EntityFunction;

/**
 * Represents an entity in an OpenStreetMap dataset.
 * Entities are a basis to model all the objects in OpenStreetMap.
 */
public interface Entity {

  /**
   * Visits the entity with the provided entity consumer.
   */
  void visit(EntityConsumer consumer) throws Exception;

  /**
   * Visits the entity with the provided entity function.
   */
  <T> T visit(EntityFunction<T> function) throws Exception;

}
