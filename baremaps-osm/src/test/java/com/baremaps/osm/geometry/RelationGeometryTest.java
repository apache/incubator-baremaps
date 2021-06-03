package com.baremaps.osm.geometry;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.MockCache;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class RelationGeometryTest {
  
  public Geometry handleRelation(String file) throws IOException {
    InputStream input = new GZIPInputStream(this.getClass().getResourceAsStream(file));
    List<Entity> entities = OpenStreetMap.streamXmlEntities(input, false).collect(Collectors.toList());
    Cache<Long, Coordinate> coordinateCache = new MockCache<>(entities.stream()
        .filter(e -> e instanceof Node)
        .map(e -> (Node) e)
        .collect(Collectors.toMap(n -> n.getId(), n -> new Coordinate(n.getLon(), n.getLat()))));
    Cache<Long, List<Long>> referenceCache = new MockCache<>(entities.stream()
        .filter(e -> e instanceof Way)
        .map(e -> (Way) e)
        .collect(Collectors.toMap(w -> w.getId(), w -> w.getNodes())));
    Relation relation = entities.stream().filter(e -> e instanceof Relation)
        .map(e -> (Relation) e)
        .findFirst().get();
    new GeometryHandler(coordinateCache, referenceCache).handle(relation);
    return relation.getGeometry();
  }
  
  @Test
  public void handleRelation381076() throws IOException {
    assertNotNull(handleRelation("/381076.osm.gz"));
  }

  @Test
  public void handleRelation1450537() throws IOException {
    assertNotNull(handleRelation("/1450537.osm.gz"));
  }

  @Test
  public void handleRelation1436294() throws IOException {
    assertNotNull(handleRelation("/1436294.osm.gz"));
  }

}
