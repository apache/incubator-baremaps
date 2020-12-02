package com.baremaps.importer.database;

import com.baremaps.osm.DefaultEntityHandler;
import com.baremaps.osm.EntityHandler;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.stream.StreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DatabaseImportHandler implements Consumer<Stream<Entity>> {

  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;

  public DatabaseImportHandler(
      HeaderTable headerTable,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable) {
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
  }

  @Override
  public void accept(Stream<Entity> entities) {
    List<Node> nodes = new ArrayList<>();
    List<Way> ways = new ArrayList<>();
    List<Relation> relations = new ArrayList<>();
    entities.forEach(new DefaultEntityHandler() {
      @Override
      public void handle(Header header) throws Exception {
        headerTable.insert(header);
      }

      @Override
      public void handle(Node node) {
        nodes.add(node);
      }

      @Override
      public void handle(Way way) {
        ways.add(way);
      }

      @Override
      public void handle(Relation relation) {
        relations.add(relation);
      }
    });
    try {
      nodeTable.copy(nodes);
      wayTable.copy(ways);
      relationTable.copy(relations);
    } catch (DatabaseException e) {
      throw new StreamException(e);
    }
  }
}
