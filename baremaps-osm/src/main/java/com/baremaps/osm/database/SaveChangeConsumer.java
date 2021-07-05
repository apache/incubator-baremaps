package com.baremaps.osm.database;

import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.ChangeConsumer;
import com.baremaps.osm.handler.EntityConsumerAdapter;

public class SaveChangeConsumer implements ChangeConsumer {

  private final EntityTable<Node> nodeTable;
  private final EntityTable<Way> wayTable;
  private final EntityTable<Relation> relationTable;

  public SaveChangeConsumer(
      EntityTable<Node> nodeTable,
      EntityTable<Way> wayTable,
      EntityTable<Relation> relationTable) {
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void match(Change change) throws Exception {
    for (Entity entity : change.getEntities()) {
      entity.visit(new EntityConsumerAdapter() {
        @Override
        public void match(Node node) throws Exception {
          switch (change.getType()) {
            case CREATE:
            case MODIFY:
              nodeTable.insert(node);
              break;
            case DELETE:
              nodeTable.delete(node.getId());
              break;
          }
        }

        @Override
        public void match(Way way) throws Exception {
          switch (change.getType()) {
            case CREATE:
            case MODIFY:
              wayTable.insert(way);
              break;
            case DELETE:
              wayTable.delete(way.getId());
              break;
          }
        }

        @Override
        public void match(Relation relation) throws Exception {
          switch (change.getType()) {
            case CREATE:
            case MODIFY:
              relationTable.insert(relation);
              break;
            case DELETE:
              relationTable.delete(relation.getId());
              break;
          }
        }
      });
    }
  }
}
