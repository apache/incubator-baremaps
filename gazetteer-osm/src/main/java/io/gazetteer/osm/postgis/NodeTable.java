package io.gazetteer.osm.postgis;

import static io.gazetteer.common.postgis.GeometryUtils.toGeometry;
import static io.gazetteer.common.postgis.GeometryUtils.toPoint;
import static io.gazetteer.common.postgis.GeometryUtils.toWKB;

import io.gazetteer.common.postgis.CopyWriter;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Point;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class NodeTable {

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, st_asbinary(ST_Transform(geom, 4326)) FROM osm_nodes WHERE id = ?";

  private static final String INSERT =
      "INSERT INTO osm_nodes (id, version, uid, timestamp, changeset, tags, geom) VALUES (?, ?, ?, ?, ?, ?, ?)";

  private static final String UPDATE =
      "UPDATE osm_nodes SET version = ?, uid = ?, timestamp = ?, changeset = ?, tags = ?, geom = ? WHERE id = ?";

  private static final String DELETE =
      "DELETE FROM osm_nodes WHERE id = ?";

  private static final String COPY = "COPY osm_nodes (id, version, uid, timestamp, changeset, tags, geom) FROM STDIN BINARY";

  public static void insert(Connection connection, Node node) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, node.getInfo().getId());
      statement.setInt(2, node.getInfo().getVersion());
      statement.setInt(3, node.getInfo().getUserId());
      statement.setObject(4, node.getInfo().getLocalDateTime());
      statement.setLong(5, node.getInfo().getChangeset());
      statement.setObject(6, node.getInfo().getTags());
      statement.setBytes(7, toWKB(toPoint(node.getLon(), node.getLat())));
      statement.execute();
    }
  }

  public static void update(Connection connection, Node node) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
      statement.setInt(1, node.getInfo().getVersion());
      statement.setInt(2, node.getInfo().getUserId());
      statement.setObject(3, node.getInfo().getLocalDateTime());
      statement.setLong(4, node.getInfo().getChangeset());
      statement.setObject(5, node.getInfo().getTags());
      statement.setBytes(6, toWKB(toPoint(node.getLon(), node.getLat())));
      statement.setLong(7, node.getInfo().getId());
      statement.execute();
    }
  }

  public static Node select(Connection connection, Long id) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        int version = result.getInt(1);
        int uid = result.getInt(2);
        long timestamp = result.getTimestamp(3).getTime();
        long changeset = result.getLong(4);
        Map<String, String> tags = (Map<String, String>) result.getObject(5);
        Point point = (Point) toGeometry(result.getBytes(6));
        return new Node(new Info(id, version, timestamp, changeset, uid, tags), point.getX(), point.getY());
      } else {
        throw new IllegalArgumentException();
      }
    }
  }

  public static void delete(Connection connection, Long id) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, id);
      statement.execute();
    }
  }

  public static void copy(PGConnection connection, List<Node> nodes) throws Exception {
    try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(connection, COPY))) {
      writer.writeHeader();
      for (Node node : nodes) {
        writer.startRow(7);
        writer.writeLong(node.getInfo().getId());
        writer.writeInteger(node.getInfo().getVersion());
        writer.writeInteger(node.getInfo().getUserId());
        writer.writeLocalDateTime(node.getInfo().getLocalDateTime());
        writer.writeLong(node.getInfo().getChangeset());
        writer.writeHstore(node.getInfo().getTags());
        writer.writeGeometry(toPoint(node.getLon(), node.getLat()));
      }
    }
  }


}
