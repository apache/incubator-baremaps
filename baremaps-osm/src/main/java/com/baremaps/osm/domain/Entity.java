package com.baremaps.osm.domain;

import com.baremaps.osm.EntityHandler;

/**
 * An interface to mark the objects of a dataset.
 */
public interface Entity {

  void accept(EntityHandler handler) throws Exception;

}
