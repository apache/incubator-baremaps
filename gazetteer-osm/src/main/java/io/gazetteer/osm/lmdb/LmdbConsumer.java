package io.gazetteer.osm.lmdb;

import io.gazetteer.osm.model.DataStoreException;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.DataBlock;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class LmdbConsumer implements Consumer<DataBlock> {

  private final LmdbStore<Long, Node> nodes;
  private final LmdbStore<Long, Way> ways;
  private final LmdbStore<Long, Relation> relations;

  public LmdbConsumer(LmdbStore<Long, Node> nodes, LmdbStore<Long, Way> ways, LmdbStore<Long, Relation> relations) {
    checkNotNull(nodes);
    checkNotNull(ways);
    checkNotNull(relations);
    this.nodes = nodes;
    this.ways = ways;
    this.relations = relations;
  }

  @Override
  public void accept(DataBlock dataBlock) {
    try {
      nodes.addAll(dataBlock.getNodes());
      ways.addAll(dataBlock.getWays());
      relations.addAll(dataBlock.getRelations());
    } catch (DataStoreException e) {
      e.printStackTrace();
    }
  }
}
