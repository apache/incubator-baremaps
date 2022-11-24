package org.apache.baremaps.openstreetmap.function;

import org.apache.baremaps.collection.LongDataMap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.List;

/**
 * A context that contains the caches used to create geometries.
 *
 * @param geometryFactory
 * @param coordinateMap
 * @param referenceMap
 */
public record Context(
  GeometryFactory geometryFactory,
  LongDataMap<Coordinate> coordinateMap,
  LongDataMap<List<Long>> referenceMap) {


}
