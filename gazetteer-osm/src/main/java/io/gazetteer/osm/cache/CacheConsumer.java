package io.gazetteer.osm.cache;

import io.gazetteer.common.postgis.GeometryUtils;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.FileBlockConsumer;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import io.gazetteer.osm.osmpbf.PrimitiveBlock;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;

public class CacheConsumer extends FileBlockConsumer {

  private final Cache<Coordinate> nodeCache;
  private final Cache<List<Long>> wayCache;
  private final Cache<List<Long>> relations;

  public CacheConsumer(Cache<Coordinate> nodeCache, Cache<List<Long>> wayCache, Cache<List<Long>> relations) {
    this.nodeCache = nodeCache;
    this.wayCache = wayCache;
    this.relations = relations;
  }

  @Override
  public void accept(HeaderBlock headerBlock) {

  }

  @Override
  public void accept(PrimitiveBlock primitiveBlock) {
    try {
      List<Node> nodes = primitiveBlock.getDenseNodes();
      List<Long> nodeKeys = nodes.stream().map(n -> n.getInfo().getId()).collect(Collectors.toList());
      List<Coordinate> nodeValues = nodes.stream().map(n -> new Coordinate(n.getLon(), n.getLat())).collect(Collectors.toList());
      nodeCache.putAll(nodeKeys, nodeValues);
      List<Way> ways = primitiveBlock.getWays();
      List<Long> wayKeys = ways.stream().map(w -> w.getInfo().getId()).collect(Collectors.toList());
      List<List<Long>> wayValues = ways.stream().map(w -> w.getNodes()).collect(Collectors.toList());
      wayCache.putAll(wayKeys, wayValues);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
