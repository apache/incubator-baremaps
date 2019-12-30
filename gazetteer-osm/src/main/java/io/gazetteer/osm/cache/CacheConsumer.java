package io.gazetteer.osm.cache;

import io.gazetteer.osm.osmpbf.FileBlockConsumer;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import io.gazetteer.osm.osmpbf.PrimitiveBlock;
import io.gazetteer.osm.store.Store;
import io.gazetteer.osm.store.Store.Entry;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;

public class CacheConsumer extends FileBlockConsumer {

  private final Store<Long, Coordinate> coordinateStore;
  private final Store<Long, List<Long>> referenceStore;

  public CacheConsumer(
      Store<Long, Coordinate> coordinateStore,
      Store<Long, List<Long>> referenceStore) {
    this.coordinateStore = coordinateStore;
    this.referenceStore = referenceStore;
  }

  @Override
  public void accept(HeaderBlock headerBlock) {
  }

  @Override
  public void accept(PrimitiveBlock primitiveBlock) {
    try {
      coordinateStore.putAll(primitiveBlock.getDenseNodes().stream()
          .map(n -> new Entry<>(n.getInfo().getId(), new Coordinate(n.getLon(), n.getLat())))
          .collect(Collectors.toList()));
      referenceStore.putAll(primitiveBlock.getWays().stream()
          .map(w -> new Entry<>(w.getInfo().getId(), w.getNodes()))
          .collect(Collectors.toList()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
