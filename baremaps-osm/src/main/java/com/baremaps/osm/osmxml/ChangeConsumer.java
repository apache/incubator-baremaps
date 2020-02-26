package com.baremaps.osm.osmxml;

import com.baremaps.osm.postgis.PostgisNodeStore;
import com.baremaps.osm.postgis.PostgisRelationStore;
import com.baremaps.osm.postgis.PostgisWayStore;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import java.util.function.Consumer;

public class ChangeConsumer implements Consumer<Change> {

  private final PostgisNodeStore nodeStore;
  private final PostgisWayStore wayStore;
  private final PostgisRelationStore relationStore;

  public ChangeConsumer(PostgisNodeStore nodeStore, PostgisWayStore wayStore, PostgisRelationStore relationStore) {
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
  }

  @Override
  public void accept(Change change) {
    Entity entity = change.getEntity();
    if (entity instanceof Node) {
      Node node = (Node) entity;
      switch (change.getType()) {
        case create:
        case modify:
          nodeStore.put(node.getInfo().getId(), node);
          break;
        case delete:
          nodeStore.delete(node.getInfo().getId());
          break;
        default:
          break;
      }
    } else if (entity instanceof Way) {
      Way way = (Way) entity;
      switch (change.getType()) {
        case create:
        case modify:
          wayStore.put(way.getInfo().getId(), way);
          break;
        case delete:
          wayStore.delete(way.getInfo().getId());
          break;
        default:
          break;
      }
    } else if (entity instanceof Relation) {
      Relation relation = (Relation) entity;
      switch (change.getType()) {
        case create:
        case modify:
          relationStore.put(relation.getInfo().getId(), relation);
          break;
        case delete:
          relationStore.delete(relation.getInfo().getId());
          break;
        default:
          break;
      }
    }
  }

}
