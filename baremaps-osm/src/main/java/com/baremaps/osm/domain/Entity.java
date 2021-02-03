package com.baremaps.osm.domain;

import com.baremaps.osm.EntityHandler;

/**
 * An interface to mark the objects of a dataset.
 */
public interface Entity {

  /**
   * A method that uses the visitor pattern to dispatch operations on entities.
   */
  void accept(EntityHandler handler) throws Exception;

}
