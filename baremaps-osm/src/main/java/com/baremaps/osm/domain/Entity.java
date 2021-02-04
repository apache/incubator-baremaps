package com.baremaps.osm.domain;

import com.baremaps.osm.handler.EntityHandler;

/**
 * An interface to mark the objects of a dataset.
 */
public abstract class Entity {

  /**
   * A method that uses the visitor pattern to dispatch operations on entities.
   */
  public abstract void accept(EntityHandler handler) throws Exception;

}
