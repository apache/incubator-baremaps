package com.baremaps.osm.database;

import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Element;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.ChangeConsumer;
import com.baremaps.osm.handler.ElementConsumer;

class DatabaseUpdateConsumer implements ChangeConsumer {

  private EntityTable<Header> headerTable;
  private EntityTable<Node> nodeTable;
  private EntityTable<Way> wayTable;
  private EntityTable<Relation> relationTable;

  public DatabaseUpdateConsumer(
      EntityTable<Header> headerTable,
      EntityTable<Node> nodeTable,
      EntityTable<Way> wayTable,
      EntityTable<Relation> relationTable) {
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void match(Change change) throws Exception {
    for (Element element : change.getElements()) {
      element.visit(new ElementConsumer() {
        @Override
        public void match(Node node) throws Exception {
          switch (change.getType()) {
            case create:
            case modify:
              nodeTable.insert(node);
              break;
            case delete:
              nodeTable.delete(node.getId());
              break;
          }
        }

        @Override
        public void match(Way way) throws Exception {
          switch (change.getType()) {
            case create:
            case modify:
              wayTable.insert(way);
              break;
            case delete:
              wayTable.delete(way.getId());
              break;
          }
        }

        @Override
        public void match(Relation relation) throws Exception {
          switch (change.getType()) {
            case create:
            case modify:
              relationTable.insert(relation);
              break;
            case delete:
              relationTable.delete(relation.getId());
              break;
          }
        }
      });
    }
  }
}
