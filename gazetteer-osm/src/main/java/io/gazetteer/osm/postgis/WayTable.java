package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Way;
import io.gazetteer.common.postgis.CopyWriter;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class WayTable {

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, nodes FROM osm_ways WHERE id = ?";

  private static final String INSERT =
      "INSERT INTO osm_ways (id, version, uid, timestamp, changeset, tags, nodes) VALUES (?, ?, ?, ?, ?, ?, ?)";

  private static final String UPDATE =
      "UPDATE osm_ways SET version = ?, uid = ?, timestamp = ?, changeset = ?, tags = ?, nodes = ? WHERE id = ?";

  private static final String DELETE =
      "DELETE FROM osm_ways WHERE id = ?";

  private static final String COPY =
      "COPY osm_ways (id, version, uid, timestamp, changeset, tags, nodes) FROM STDIN BINARY";

  public static void insert(Connection connection, Way way) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, way.getInfo().getId());
      statement.setInt(2, way.getInfo().getVersion());
      statement.setInt(3, way.getInfo().getUserId());
      statement.setObject(4, way.getInfo().getTimestamp());
      statement.setLong(5, way.getInfo().getChangeset());
      statement.setObject(6, way.getInfo().getTags());
      statement.setObject(7, way.getNodes().stream().mapToLong(Long::longValue).toArray());
      statement.execute();
    }
  }

  public static void update(Connection connection, Way way) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
      statement.setInt(1, way.getInfo().getVersion());
      statement.setInt(2, way.getInfo().getUserId());
      statement.setObject(3, way.getInfo().getTimestamp());
      statement.setLong(4, way.getInfo().getChangeset());
      statement.setObject(5, way.getInfo().getTags());
      statement.setObject(6, way.getNodes().stream().mapToLong(Long::longValue).toArray());
      statement.setLong(7, way.getInfo().getId());
      statement.execute();
    }
  }

  public static Way select(Connection connection, Long id) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(SELECT)) {
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
    }
  }

  public static void delete(Connection connection, Long id) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, id);
      statement.execute();
    }
  }
}
