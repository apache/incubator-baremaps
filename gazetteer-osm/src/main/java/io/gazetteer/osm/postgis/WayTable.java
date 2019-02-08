package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.DataStore;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.gazetteer.osm.postgis.GeometryUtil.asGeometryWithWrappedException;
import static io.gazetteer.osm.postgis.GeometryUtil.asWKB;

public class WayTable implements DataTable<Long, Way> {

  public static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, nodes FROM osm_ways WHERE id = ?";

  public static final String INSERT =
      "INSERT INTO osm_ways (id, version, uid, timestamp, changeset, tags, nodes, geom) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

  public static final String UPDATE =
      "UPDATE osm_ways SET version = ?, uid = ?, timestamp = ?, changeset = ?, tags = ?, nodes = ?, geom = ? WHERE id = ?";

  public static final String DELETE = "DELETE FROM osm_ways WHERE id = ?";

  public final DataStore<Long, Node> nodeStore;

  public WayTable(DataStore<Long, Node> nodeStore) {
    this.nodeStore = nodeStore;
  }

  @Override
  public void insert(Connection connection, Way way) throws SQLException {
    Array nodes = connection.createArrayOf("bigint", way.getNodes().toArray());
    PreparedStatement statement = connection.prepareStatement(INSERT);
    statement.setLong(1, way.getInfo().getId());
    statement.setInt(2, way.getInfo().getVersion());
    statement.setInt(3, way.getInfo().getUserId());
    statement.setTimestamp(4, new Timestamp(way.getInfo().getTimestamp()));
    statement.setLong(5, way.getInfo().getChangeset());
    statement.setObject(6, way.getInfo().getTags());
    statement.setArray(7, nodes);
    statement.setBytes(8, asWKB(asGeometryWithWrappedException(way, nodeStore)));
    statement.execute();
    nodes.free();
  }

  @Override
  public void update(Connection connection, Way way) throws SQLException {
    Array nodes = connection.createArrayOf("bigint", way.getNodes().toArray());
    PreparedStatement statement = connection.prepareStatement(UPDATE);
    statement.setInt(1, way.getInfo().getVersion());
    statement.setInt(2, way.getInfo().getUserId());
    statement.setTimestamp(3, new Timestamp(way.getInfo().getTimestamp()));
    statement.setLong(4, way.getInfo().getChangeset());
    statement.setObject(5, way.getInfo().getTags());
    statement.setArray(6, nodes);
    statement.setBytes(7, asWKB(asGeometryWithWrappedException(way, nodeStore)));
    statement.setLong(8, way.getInfo().getId());
    statement.execute();
    nodes.free();
  }

  @Override
  public Way select(Connection connection, Long id) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(SELECT);
    statement.setLong(1, id);
    ResultSet result = statement.executeQuery();
    if (result.next()) {
      int version = result.getInt(1);
      int uid = result.getInt(2);
      long timestamp = result.getTimestamp(3).getTime();
      long changeset = result.getLong(4);
      Map<String, String> tags = (Map<String, String>) result.getObject(5);
      Array array = result.getArray(6);
      List<Long> nodes = null;
      if (array != null) {
        nodes = Arrays.asList((Long[]) array.getArray());
      } else {
        nodes = new ArrayList<>();
      }
      return new Way(new Info(id, version, timestamp, changeset, uid, tags), nodes);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void delete(Connection connection, Long id) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(DELETE);
    statement.setLong(1, id);
    statement.execute();
  }
}
