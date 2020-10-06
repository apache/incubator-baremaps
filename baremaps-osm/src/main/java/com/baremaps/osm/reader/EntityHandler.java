package com.baremaps.osm.reader;

import com.baremaps.osm.model.Header;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;

public interface EntityHandler {

  void onHeader(Header header) throws Exception;

  void onNode(Node node) throws Exception;

  void onWay(Way way) throws Exception;

  void onRelation(Relation relation) throws Exception;

}
