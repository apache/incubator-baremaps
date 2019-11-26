package io.gazetteer.osm.osmxml;

import static io.gazetteer.osm.osmxml.XMLConstants.CREATE;
import static io.gazetteer.osm.osmxml.XMLConstants.DELETE;
import static io.gazetteer.osm.osmxml.XMLConstants.MODIFY;

import io.gazetteer.osm.model.Entity;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Store;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.postgis.PostgisNodeStore;
import io.gazetteer.osm.postgis.PostgisRelationStore;
import io.gazetteer.osm.postgis.PostgisWayStore;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class ChangeConsumer implements Consumer<Change> {

  private final DataSource datasource;

  private final PostgisNodeStore nodeStore;
  private final PostgisWayStore wayStore;
  private final PostgisRelationStore relationStore;

  public ChangeConsumer(DataSource datasource, PostgisNodeStore nodeStore, PostgisWayStore wayStore, PostgisRelationStore relationStore) {
    this.datasource = datasource;
    this.nodeStore = nodeStore;
    this.wayStore = wayStore;
    this.relationStore = relationStore;
  }

  @Override
  public void accept(Change change) {
    try (Connection connection = datasource.getConnection()) {
      Entity entity = change.getEntity();
      if (entity instanceof Node) {
        Node node = (Node) entity;
        switch (change.getType()) {
          case CREATE:
          case MODIFY:
            nodeStore.put(node.getInfo().getId(), node);
            break;
          case DELETE:
            nodeStore.delete(node.getInfo().getId());
            break;
          default:
            break;
        }
      } else if (entity instanceof Way) {
        Way way = (Way) entity;
        switch (change.getType()) {
          case CREATE:
          case MODIFY:
            wayStore.put(way.getInfo().getId(), way);
            break;
          case DELETE:
            wayStore.delete(way.getInfo().getId());
            break;
          default:
            break;
        }
      } else if (entity instanceof Relation) {
        Relation relation = (Relation) entity;
        switch (change.getType()) {
          case CREATE:
          case MODIFY:
            relationStore.put(relation.getInfo().getId(), relation);
            break;
          case DELETE:
            relationStore.delete(relation.getInfo().getId());
            break;
          default:
            break;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public <T> void test(Store<Long, T> store, T entity) {


  }

}
