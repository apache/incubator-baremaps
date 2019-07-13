package io.gazetteer.osm.osmxml;

import static io.gazetteer.osm.osmxml.XMLConstants.CREATE;
import static io.gazetteer.osm.osmxml.XMLConstants.DELETE;
import static io.gazetteer.osm.osmxml.XMLConstants.MODIFY;

import io.gazetteer.osm.model.Change;
import io.gazetteer.osm.model.Entity;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.postgis.NodeTable;
import io.gazetteer.osm.postgis.RelationTable;
import io.gazetteer.osm.postgis.WayTable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import org.apache.commons.dbcp2.PoolingDataSource;

public class ChangeConsumer implements Consumer<Change> {

  private final PoolingDataSource pool;

  public ChangeConsumer(PoolingDataSource pool) {
    this.pool = pool;
  }

  @Override
  public void accept(Change change) {
    try (Connection connection = pool.getConnection()) {
      Entity entity = change.getEntity();
      if (entity instanceof Node) {
        Node node = (Node) entity;
        switch (change.getType()) {
          case CREATE:
            NodeTable.insert(connection, node);
            break;
          case MODIFY:
            NodeTable.update(connection, node);
            break;
          case DELETE:
            NodeTable.delete(connection, node.getInfo().getId());
            break;
          default:
            break;
        }
      } else if (entity instanceof Way) {
        Way way = (Way) entity;
        switch (change.getType()) {
          case CREATE:
            WayTable.insert(connection, way);
            break;
          case MODIFY:
            WayTable.update(connection, way);
            break;
          case DELETE:
            WayTable.delete(connection, way.getInfo().getId());
            break;
          default:
            break;
        }
      } else if (entity instanceof Relation) {
        Relation relation = (Relation) entity;
        switch (change.getType()) {
          case CREATE:
            RelationTable.insert(connection, relation);
            break;
          case MODIFY:
            RelationTable.update(connection, relation);
            break;
          case DELETE:
            RelationTable.delete(connection, relation.getInfo().getId());
            break;
          default:
            break;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


}
