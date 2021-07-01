package com.baremaps.osm.domain;

import com.baremaps.osm.handler.EntityConsumer;
import com.baremaps.osm.handler.EntityFunction;

/**
 * An interface to mark the objects of a dataset.
 */
public abstract class Entity {

  /**
   * A method that uses the visitor pattern to handle entities.
   */
  public abstract void visit(EntityConsumer handler) throws Exception;

  /**
   * A method that uses the visitor pattern to map entities.
   */
  public abstract <T> T visit(EntityFunction<T> mapper) throws Exception;

}
