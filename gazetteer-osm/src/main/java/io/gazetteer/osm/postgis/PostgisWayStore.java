package io.gazetteer.osm.postgis;

import io.gazetteer.core.postgis.CopyWriter;
import io.gazetteer.osm.geometry.GeometryUtil;
import io.gazetteer.osm.geometry.WayBuilder;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.store.Store;
import io.gazetteer.osm.store.StoreException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgisWayStore implements Store<Long, Way> {

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, nodes FROM osm_ways WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, version, uid, timestamp, changeset, tags, nodes FROM osm_ways WHERE id = ANY (?)";

  private static final String INSERT =
      "INSERT INTO osm_ways (id, version, uid, timestamp, changeset, tags, nodes, geom) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
          + "ON CONFLICT (id) DO UPDATE SET "
          + "version = excluded.version, "
          + "uid = excluded.uid, "
          + "timestamp = excluded.timestamp, "
          + "changeset = excluded.changeset, "
          + "tags = excluded.tags, "
          + "nodes = excluded.nodes, "
          + "geom = excluded.geom";

  private static final String DELETE = "DELETE FROM osm_ways WHERE id = ?";

  private static final String COPY =
      "COPY osm_ways (id, version, uid, timestamp, changeset, tags, nodes, geom) FROM STDIN BINARY";

  private final DataSource dataSource;

  private final WayBuilder wayBuilder;

  public PostgisWayStore(DataSource dataSource, WayBuilder wayBuilder) {
    this.dataSource = dataSource;
    this.wayBuilder = wayBuilder;
  }

  public Way get(Long id) {
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
        List<Long> nodes = new ArrayList<>();
        Array array = result.getArray(6);
        if (array != null) {
          nodes = Arrays.asList((Long[]) array.getArray());
        }
        return new Way(new Info(id, version, timestamp, changeset, uid, tags), nodes);
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public List<Way> getAll(List<Long> keys) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Way> ways = new HashMap<>();
      while (result.next()) {
        long id = result.getLong(1);
        int version = result.getInt(2);
        int uid = result.getInt(3);
        LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
        long changeset = result.getLong(5);
        Map<String, String> tags = (Map<String, String>) result.getObject(6);
        List<Long> nodes = new ArrayList<>();
        Array array = result.getArray(7);
        if (array != null) {
          nodes = Arrays.asList((Long[]) array.getArray());
        }
        ways.put(id, new Way(new Info(id, version, timestamp, changeset, uid, tags), nodes));
      }
      return keys.stream().map(key -> ways.get(key)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(Long key, Way value) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, key);
      statement.setInt(2, value.getInfo().getVersion());
      statement.setInt(3, value.getInfo().getUserId());
      statement.setObject(4, value.getInfo().getTimestamp());
      statement.setLong(5, value.getInfo().getChangeset());
      statement.setObject(6, value.getInfo().getTags());
      statement.setObject(7, value.getNodes().stream().mapToLong(Long::longValue).toArray());
      byte[] wkb = wayBuilder != null ? GeometryUtil.serialize(wayBuilder.build(value)) : null;
      statement.setBytes(8, wkb);
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void putAll(List<Entry<Long, Way>> entries) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      for (Entry<Long, Way> entry : entries) {
        Long key = entry.key();
        Way value = entry.value();
        statement.clearParameters();
        statement.setLong(1, key);
        statement.setInt(2, value.getInfo().getVersion());
        statement.setInt(3, value.getInfo().getUserId());
        statement.setObject(4, value.getInfo().getTimestamp());
        statement.setLong(5, value.getInfo().getChangeset());
        statement.setObject(6, value.getInfo().getTags());
        statement.setObject(7, value.getNodes().stream().mapToLong(Long::longValue).toArray());
        byte[] wkb = wayBuilder != null ? GeometryUtil.serialize(wayBuilder.build(value)) : null;
        statement.setBytes(8, wkb);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void delete(Long key) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, key);
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void deleteAll(List<Long> keys) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      for (Long key : keys) {
        statement.clearParameters();
        statement.setLong(1, key);
        statement.execute();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void importAll(List<Entry<Long, Way>> entries) {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
        writer.writeHeader();
        for (Entry<Long, Way> entry : entries) {
          Long key = entry.key();
          Way way = entry.value();
          writer.startRow(8);
          writer.writeLong(key);
          writer.writeInteger(way.getInfo().getVersion());
          writer.writeInteger(way.getInfo().getUserId());
          writer.writeLocalDateTime(way.getInfo().getTimestamp());
          writer.writeLong(way.getInfo().getChangeset());
          writer.writeHstore(way.getInfo().getTags());
          writer.writeLongList(way.getNodes());
          writer.writeGeometry(wayBuilder.build(way));
        }
      }
    } catch (Exception e) {
      throw new StoreException(e);
    }
  }
}
