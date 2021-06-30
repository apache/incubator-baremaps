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
 * A consumer that uses the visitor pattern to handle entities according to their type.
 */
public interface EntityHandler extends Consumer<Entity> {

  @Override
  default void accept(Entity entity) {
    try {
      entity.accept(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  void handle(Header header) throws Exception;

  void handle(Bound bound) throws Exception;

  void handle(Node node) throws Exception;

  void handle(Way way) throws Exception;

  void handle(Relation relation) throws Exception;

}
