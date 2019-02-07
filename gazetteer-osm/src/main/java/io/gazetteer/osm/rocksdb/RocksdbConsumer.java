package io.gazetteer.osm.rocksdb;

import io.gazetteer.osm.model.EntityStoreException;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.DataBlock;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class RocksdbConsumer implements Consumer<DataBlock> {

  private final RocksdbEntityStore<Node> nodeStore;
  private final RocksdbEntityStore<Way> wayStore;

  public RocksdbConsumer(RocksdbEntityStore<Node> nodeStore, RocksdbEntityStore<Way> wayStore) {
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
    } catch (EntityStoreException e) {
      e.printStackTrace();
    }
  }
}
