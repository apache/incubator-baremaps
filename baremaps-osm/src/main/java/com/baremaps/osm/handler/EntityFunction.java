package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.stream.StreamException;
import java.util.function.Function;

/**
 * Represents a function that transforms entities of different types.
 *
 * @param <T>
 */
public interface EntityFunction<T> extends Function<Entity, T> {

  @Override
  default T apply(Entity entity) {
    try {
      return entity.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  T match(Header header) throws Exception;

  T match(Bound bound) throws Exception;

  T match(Node node) throws Exception;

  T match(Way way) throws Exception;

  T match(Relation relation) throws Exception;

}
