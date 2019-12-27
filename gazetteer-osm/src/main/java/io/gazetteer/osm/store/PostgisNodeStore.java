package io.gazetteer.osm.store;

import io.gazetteer.osm.geometry.NodeBuilder;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.postgis.CopyWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Point;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgisNodeStore implements Store<Long, Node> {

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, st_asbinary(ST_Transform(geom, 4326)) FROM osm_nodes WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, version, uid, timestamp, changeset, tags, st_asbinary(ST_Transform(geom, 4326)) FROM osm_nodes WHERE id = ANY (?)";

  private static final String INSERT =
      "INSERT INTO osm_nodes (id, version, uid, timestamp, changeset, tags, geom) VALUES (?, ?, ?, ?, ?, ?, ?)"
          + "ON CONFLICT (id) DO UPDATE SET "
          + "version = excluded.version, "
          + "uid = excluded.uid, "
          + "timestamp = excluded.timestamp, "
          + "changeset = excluded.changeset, "
          + "tags = excluded.tags, "
          + "geom = excluded.geom";

  private static final String DELETE = "DELETE FROM osm_nodes WHERE id = ?";

  private static final String COPY =
      "COPY osm_nodes (id, version, uid, timestamp, changeset, tags, geom) FROM STDIN BINARY";

  private final DataSource dataSource;

  private final NodeBuilder nodeBuilder;

  public PostgisNodeStore(DataSource dataSource, NodeBuilder nodeBuilder) {
    this.dataSource = dataSource;
    this.nodeBuilder = nodeBuilder;
  }

  public Node get(Long id) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        int version = result.getInt(1);
        int uid = result.getInt(2);
        LocalDateTime timestamp = result.getObject(3, LocalDateTime.class);
        long changeset = result.getLong(4);
        Map<String, String> tags = (Map<String, String>) result.getObject(5);
        Point point = (Point) deserialize(result.getBytes(6));
        return new Node(
            new Info(id, version, timestamp, changeset, uid, tags), point.getX(), point.getY());
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public List<Node> getAll(List<Long> keys) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Node> nodes = new HashMap<>();
      while (result.next()) {
        long id = result.getInt(1);
        int version = result.getInt(2);
        int uid = result.getInt(3);
        LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
        long changeset = result.getLong(5);
        Map<String, String> tags = (Map<String, String>) result.getObject(6);
        Point point = (Point) deserialize(result.getBytes(7));
        nodes.put(
            id,
            new Node(
                new Info(id, version, timestamp, changeset, uid, tags),
                point.getX(),
                point.getY()));
      }
      return keys.stream().map(key -> nodes.get(key)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(Long key, Node value) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, value.getInfo().getId());
      statement.setInt(2, value.getInfo().getVersion());
      statement.setInt(3, value.getInfo().getUserId());
      statement.setObject(4, value.getInfo().getTimestamp());
      statement.setLong(5, value.getInfo().getChangeset());
      statement.setObject(6, value.getInfo().getTags());
      byte[] wkb = nodeBuilder != null ? serialize(nodeBuilder.build(value)) : null;
      statement.setBytes(7, wkb);
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void putAll(List<StoreEntry<Long, Node>> entries) {
    for (StoreEntry<Long, Node> entry : entries) {
      put(entry.key(), entry.value());
    }
  }

  public void delete(Long id) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, id);
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void deleteAll(List<Long> keys) {
    for (Long key : keys) {
      delete(key);
    }
  }

  public void importAll(List<StoreEntry<Long, Node>> entries) {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
        writer.writeHeader();
        for (StoreEntry<Long, Node> entry : entries) {
          Long id = entry.key();
          Node node = entry.value();
          writer.startRow(7);
          writer.writeLong(id);
          writer.writeInteger(node.getInfo().getVersion());
          writer.writeInteger(node.getInfo().getUserId());
          writer.writeLocalDateTime(node.getInfo().getTimestamp());
          writer.writeLong(node.getInfo().getChangeset());
          writer.writeHstore(node.getInfo().getTags());
          writer.writeGeometry(nodeBuilder.build(node));
        }
      }
    } catch (Exception e) {
      throw new StoreException(e);
    }
  }
}
