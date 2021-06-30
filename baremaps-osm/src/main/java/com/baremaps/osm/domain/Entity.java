package com.baremaps.osm.domain;

import com.baremaps.osm.handler.EntityHandler;
import com.baremaps.osm.handler.EntityMapper;

/**
 * An interface to mark the objects of a dataset.
 */
public abstract class Entity {

  /**
   * A method that uses the visitor pattern to handle entities.
   */
  public abstract void accept(EntityHandler handler) throws Exception;

  /**
   * A method that uses the visitor pattern to map entities.
   */
  public abstract <T> T accept(EntityMapper<T> mapper) throws Exception;

}
