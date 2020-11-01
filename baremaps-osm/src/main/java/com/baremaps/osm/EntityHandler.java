package com.baremaps.osm;

import com.baremaps.osm.domain.Bounds;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.stream.StreamException;
import java.util.function.Consumer;

public interface EntityHandler extends Consumer<Entity> {

  @Override
  default void accept(Entity entity) {
    try {
      entity.visit(this);
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  void handle(Header header) throws Exception;

  void handle(Bounds bounds) throws Exception;

  void handle(Node node) throws Exception;

  void handle(Way way) throws Exception;

  void handle(Relation relation) throws Exception;

}
