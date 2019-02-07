package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.User;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static io.gazetteer.osm.postgis.GeometryUtil.asGeometry;
import static io.gazetteer.osm.postgis.GeometryUtil.asWKB;
import static io.gazetteer.osm.postgis.HstoreUtil.asHstore;
import static io.gazetteer.osm.postgis.HstoreUtil.asMap;

public class NodeTable implements EntityTable<Node> {

  public static final String DROP_TABLE_NODES = "DROP TABLE IF EXISTS osm_nodes";

  public static final String CREATE_TABLE_NODES =
          "CREATE TABLE IF NOT EXISTS osm_nodes ("
                  + "id bigint NOT NULL,"
                  + "version int NOT NULL,"
                  + "uid int NOT NULL,"
                  + "timestamp timestamp without time zone NOT NULL,"
                  + "changeset bigint NOT NULL,"
                  + "tags hstore,"
                  + "geom geometry(point)"
                  + ")";

  public static final String CREATE_INDEX_NODES =
          "CREATE INDEX IF NOT EXISTS osm_nodes_idx ON osm_nodes USING gist(geom)";

  public static final String SELECT_NODE =
      "SELECT version, uid, timestamp, changeset, tags, st_asbinary(geom) FROM osm_nodes WHERE id = ?";

  public static final String INSERT_NODE =
      "INSERT INTO osm_nodes (id, version, uid, timestamp, changeset, tags, geom) VALUES (?, ?, ?, ?, ?, ?, ?)";

  public static final String UPDATE_NODE =
      "UPDATE osm_nodes SET version = ?, uid = ?, timestamp = ?, changeset = ?, tags = ?, geom = ? WHERE id = ?";

  public static final String DELETE_NODE = "DELETE FROM osm_nodes WHERE id = ?";

  @Override
  public void insert(Connection connection, Node node) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(INSERT_NODE);
    statement.setLong(1, node.getInfo().getId());
    statement.setInt(2, node.getInfo().getVersion());
    statement.setInt(3, node.getInfo().getUser().getId());
    statement.setLong(4, node.getInfo().getTimestamp());
    statement.setLong(5, node.getInfo().getChangeset());
    statement.setBytes(6, asHstore(node.getInfo().getTags()));
    statement.setBytes(7, asWKB(asGeometry(node)));
    statement.execute();
  }

  @Override
  public void update(Connection connection, Node node) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(UPDATE_NODE);
    statement.setInt(1, node.getInfo().getVersion());
    statement.setInt(2, node.getInfo().getUser().getId());
    statement.setLong(3, node.getInfo().getTimestamp());
    statement.setLong(4, node.getInfo().getChangeset());
    statement.setBytes(5, asHstore(node.getInfo().getTags()));
    statement.setBytes(6, asWKB(asGeometry(node)));
    statement.setLong(7, node.getInfo().getId());
    statement.execute();
  }

  @Override
  public Node select(Connection connection, long id) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(SELECT_NODE);
    statement.setLong(1, id);
    ResultSet result = statement.executeQuery();
    if (result.next()) {
      int version = result.getInt(1);
      int uid = result.getInt(2);
      long timestamp = result.getLong(3);
      long changeset = result.getInt(4);
      Map<String, String> tags = asMap(result.getBytes(5));
      Point point = (Point) asGeometry(result.getBytes(6));
      return new Node(new Info(id, version, timestamp, changeset, new User(uid, null), tags), point.getX(), point.getY());
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void delete(Connection connection, long id) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(DELETE_NODE);
    statement.setLong(1, id);
    statement.execute();
  }
}
