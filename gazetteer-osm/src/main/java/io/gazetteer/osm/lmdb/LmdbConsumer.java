package io.gazetteer.osm.lmdb;

import io.gazetteer.osm.model.DataStoreException;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.DataBlock;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class LmdbConsumer implements Consumer<DataBlock> {

  private final LmdbStore<Long, Node> nodeStore;
  private final LmdbStore<Long, Way> wayStore;

  public LmdbConsumer(LmdbStore<Long, Node> nodeStore, LmdbStore<Long, Way> wayStore) {
    checkNotNull(nodeStore);
    checkNotNull(wayStore);
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
  }

  @Override
  public void accept(DataBlock dataBlock) {
    try {
      nodeStore.addAll(dataBlock.getNodes());
      wayStore.addAll(dataBlock.getWays());
    } catch (DataStoreException e) {
      e.printStackTrace();
    }
  }
}
