package com.baremaps.osm.handler;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.stream.StreamException;
import java.util.function.Function;

public interface EntityMapper<T> extends Function<Entity, T> {

  @Override
  default T apply(Entity entity) {
    try {
      return entity.accept(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  T map(Header header) throws Exception;

  T map(Bound bound) throws Exception;

  T map(Node node) throws Exception;

  T map(Way way) throws Exception;

  T map(Relation relation) throws Exception;

}
