package com.baremaps.osm.geometry;

import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.EntityFunction;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;

public class ExtractGeometryFunction implements EntityFunction<Optional<Geometry>> {

  @Override
  public Optional<Geometry> match(Header header) throws Exception {
    return Optional.empty();
  }

  @Override
  public Optional<Geometry> match(Bound bound) throws Exception {
    return Optional.empty();
  }

  @Override
  public Optional<Geometry> match(Node node) throws Exception {
    return Optional.ofNullable(node.getGeometry());
  }

  @Override
  public Optional<Geometry> match(Way way) throws Exception {
    return Optional.ofNullable(way.getGeometry());
  }

  @Override
  public Optional<Geometry> match(Relation relation) throws Exception {
    return Optional.ofNullable(relation.getGeometry());
  }

}
