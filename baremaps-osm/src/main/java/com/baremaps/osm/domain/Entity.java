package com.baremaps.osm.domain;

import com.baremaps.osm.EntityHandler;

/**
 * An interface to mark the objects of a dataset.
 */
public interface Entity {

  void visit(EntityHandler visitor) throws Exception;

}
