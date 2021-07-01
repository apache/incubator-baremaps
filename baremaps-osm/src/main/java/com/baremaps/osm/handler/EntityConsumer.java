package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.stream.StreamException;
import java.util.function.Consumer;

/**
 * Represents an operation on entities of different types.
 */
public interface EntityConsumer extends Consumer<Entity> {

  @Override
  default void accept(Entity entity) {
    try {
      entity.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  void match(Header header) throws Exception;

  void match(Bound bound) throws Exception;

  void match(Node node) throws Exception;

  void match(Way way) throws Exception;

  void match(Relation relation) throws Exception;

}
