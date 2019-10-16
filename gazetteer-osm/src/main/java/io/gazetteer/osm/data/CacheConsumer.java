package io.gazetteer.osm.data;

import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.FileBlockConsumer;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import io.gazetteer.osm.osmpbf.PrimitiveBlock;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;

public class CacheConsumer extends FileBlockConsumer {

  private final FixedSizeObjectMap<Coordinate> coordinatesMap;

  private final VariableSizeObjectMap<List<Long>> referencesMap;

  private final Long2ObjectOpenHashMap map = new Long2ObjectOpenHashMap<>();

  public CacheConsumer(FixedSizeObjectMap<Coordinate> coordinatesMap, VariableSizeObjectMap<List<Long>> referencesMap) {
    this.coordinatesMap = coordinatesMap;
    this.referencesMap = referencesMap;
  }

  @Override
  public void accept(HeaderBlock headerBlock) {

  }

  @Override
  public void accept(PrimitiveBlock primitiveBlock) {
    try {
      for (Node node : primitiveBlock.getDenseNodes()) {
        long id = node.getInfo().getId();
        Coordinate value = new Coordinate(node.getLon(), node.getLat());
        coordinatesMap.set(id, value);
      }
      for (Way way : primitiveBlock.getWays()) {
        long id = way.getInfo().getId();
        List<Long> references = way.getNodes();
        referencesMap.set(id, references);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
