package io.gazetteer.osm.rocksdb;

import io.gazetteer.osm.model.DataStoreException;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.DataBlock;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class RocksdbConsumer implements Consumer<DataBlock> {

  private final RocksdbStore<Long, Node> nodeStore;
  private final RocksdbStore<Long, Way> wayStore;

  public RocksdbConsumer(RocksdbStore<Long, Node> nodeStore, RocksdbStore<Long, Way> wayStore) {
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
