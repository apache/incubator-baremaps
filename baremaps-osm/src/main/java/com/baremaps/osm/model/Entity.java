package com.baremaps.osm.model;

import com.baremaps.osm.EntityHandler;

public interface Entity {

  void visit(EntityHandler visitor) throws Exception;

}
