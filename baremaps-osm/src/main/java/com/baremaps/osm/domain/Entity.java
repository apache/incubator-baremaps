package com.baremaps.osm.domain;

import com.baremaps.osm.EntityHandler;

public interface Entity {

  void visit(EntityHandler visitor) throws Exception;

}
