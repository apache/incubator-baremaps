package io.gazetteer.osm.rocksdb;

import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.domain.Way;
import io.gazetteer.osm.osmpbf.DataBlock;

import java.util.Collection;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class RocksdbConsumer implements Consumer<DataBlock> {

  private final EntityStore<Node> nodeStore;
  private final EntityStore<Way> wayStore;

  public RocksdbConsumer(EntityStore<Node> nodeStore, EntityStore<Way> wayStore) {
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
