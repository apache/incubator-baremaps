package com.baremaps.importer.database;

import com.baremaps.osm.ChangeHandler;
import com.baremaps.osm.ElementHandler;
import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Element;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateHandler implements ChangeHandler, AutoCloseable {

  private static Logger logger = LoggerFactory.getLogger(UpdateHandler.class);

  private final HeaderTable headerTable;

  private final NodeTable nodeTable;

  private final WayTable wayTable;

  private final RelationTable relationTable;

  public UpdateHandler(HeaderTable headerTable, NodeTable nodeTable, WayTable wayTable, RelationTable relationTable) {
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void handle(Change change) throws Exception {
    for (Element element : change.getElements()) {
      element.accept(new ElementHandler() {
        @Override
        public void handle(Node node) throws Exception {
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
        public void handle(Way way) throws Exception {
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
        public void handle(Relation relation) throws Exception {
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

  @Override
  public void close() {

  }
}
