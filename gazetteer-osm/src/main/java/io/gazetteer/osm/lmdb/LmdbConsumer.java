package io.gazetteer.osm.lmdb;

import io.gazetteer.osm.model.Store;
import io.gazetteer.osm.osmpbf.FileBlockConsumer;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import io.gazetteer.osm.osmpbf.PrimitiveBlock;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;

public class LmdbConsumer extends FileBlockConsumer {

  private final Store<Long, Coordinate> coordinateStore;
  private final Store<Long, List<Long>> referenceStore;

  public LmdbConsumer(
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
          .map(n -> new Store.Entry<>(n.getInfo().getId(), new Coordinate(n.getLon(), n.getLat())))
          .collect(Collectors.toList()));
      referenceStore.putAll(primitiveBlock.getWays().stream()
          .map(w -> new Store.Entry<>(w.getInfo().getId(), w.getNodes()))
          .collect(Collectors.toList()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
