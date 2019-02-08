package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import org.locationtech.jts.geom.Point;

import java.sql.*;
import java.util.Map;

import static io.gazetteer.osm.postgis.GeometryUtil.asGeometry;
import static io.gazetteer.osm.postgis.GeometryUtil.asWKB;

public class NodeTable implements PostgisTable<Long, Node> {

  public static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, st_asbinary(geom) FROM osm_nodes WHERE id = ?";

  public static final String INSERT =
      "INSERT INTO osm_nodes (id, version, uid, timestamp, changeset, tags, geom) VALUES (?, ?, ?, ?, ?, ?, ?)";

  public static final String UPDATE =
      "UPDATE osm_nodes SET version = ?, uid = ?, timestamp = ?, changeset = ?, tags = ?, geom = ? WHERE id = ?";

  public static final String DELETE = "DELETE FROM osm_nodes WHERE id = ?";

  @Override
  public void insert(Connection connection, Node node) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(INSERT);
    statement.setLong(1, node.getInfo().getId());
    statement.setInt(2, node.getInfo().getVersion());
    statement.setInt(3, node.getInfo().getUserId());
    statement.setTimestamp(4, new Timestamp(node.getInfo().getTimestamp()));
    statement.setLong(5, node.getInfo().getChangeset());
    statement.setObject(6, node.getInfo().getTags());
    statement.setBytes(7, asWKB(asGeometry(node)));
    statement.execute();
  }

  @Override
  public void update(Connection connection, Node node) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(UPDATE);
    statement.setInt(1, node.getInfo().getVersion());
    statement.setInt(2, node.getInfo().getUserId());
    statement.setTimestamp(3, new Timestamp(node.getInfo().getTimestamp()));
    statement.setLong(4, node.getInfo().getChangeset());
    statement.setObject(5, node.getInfo().getTags());
    statement.setBytes(6, asWKB(asGeometry(node)));
    statement.setLong(7, node.getInfo().getId());
    statement.execute();
  }

  @Override
  public Node select(Connection connection, Long id) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(SELECT);
    statement.setLong(1, id);
    ResultSet result = statement.executeQuery();
    if (result.next()) {
      int version = result.getInt(1);
      int uid = result.getInt(2);
      long timestamp = result.getTimestamp(3).getTime();
      long changeset = result.getLong(4);
      Map<String, String> tags = (Map<String, String>) result.getObject(5);
      Point point = (Point) asGeometry(result.getBytes(6));
      return new Node(
          new Info(id, version, timestamp, changeset, uid, tags), point.getX(), point.getY());
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
