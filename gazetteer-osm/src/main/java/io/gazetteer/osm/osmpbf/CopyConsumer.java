package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.model.StoreEntry;
import io.gazetteer.osm.postgis.PostgisHeaderStore;
import io.gazetteer.osm.postgis.PostgisNodeStore;
import io.gazetteer.osm.postgis.PostgisRelationStore;
import io.gazetteer.osm.postgis.PostgisWayStore;
import java.util.stream.Collectors;

public class CopyConsumer extends FileBlockConsumer {

  private final PostgisHeaderStore headerStore;
  private final PostgisNodeStore nodeStore;
  private final PostgisWayStore wayStore;
  private final PostgisRelationStore relationStore;

  public CopyConsumer(PostgisHeaderStore headerStore, PostgisNodeStore nodeStore, PostgisWayStore wayStore, PostgisRelationStore relationStore) {
    this.headerStore = headerStore;
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
  }

  @Override
  public void accept(HeaderBlock headerBlock) {
    try {
      headerStore.insert(headerBlock);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void accept(PrimitiveBlock primitiveBlock) {
    try {
      nodeStore.importAll(primitiveBlock.getDenseNodes().stream()
          .map(node -> new StoreEntry<>(node.getInfo().getId(), node))
          .collect(Collectors.toList()));
      wayStore.importAll(primitiveBlock.getWays().stream()
          .map(way -> new StoreEntry<>(way.getInfo().getId(), way))
          .collect(Collectors.toList()));
      relationStore.importAll(primitiveBlock.getRelations().stream()
          .map(relation -> new StoreEntry<>(relation.getInfo().getId(), relation))
          .collect(Collectors.toList()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
