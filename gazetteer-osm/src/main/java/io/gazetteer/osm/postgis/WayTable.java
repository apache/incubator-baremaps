package io.gazetteer.osm.postgis;

import static io.gazetteer.osm.util.GeometryUtil.asGeometryWithWrappedException;
import static io.gazetteer.osm.util.GeometryUtil.asWKB;

import io.gazetteer.osm.model.DataStore;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Way;
import io.gazetteer.postgis.util.CopyWriter;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

  private static final String COPY_WAYS =
      "COPY osm_ways (id, version, uid, timestamp, changeset, tags, nodes) FROM STDIN BINARY";

  private final DataStore<Long, Node> nodeStore;

  public WayTable(DataStore<Long, Node> nodeStore) {
    this.nodeStore = nodeStore;
  }

  public void insert(Connection connection, Way way) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, way.getInfo().getId());
      statement.setInt(2, way.getInfo().getVersion());
      statement.setInt(3, way.getInfo().getUserId());
      statement.setTimestamp(4, new Timestamp(way.getInfo().getTimestamp()));
      statement.setLong(5, way.getInfo().getChangeset());
      statement.setObject(6, way.getInfo().getTags());
      statement.setObject(7, way.getNodes().toArray(new Long[0]));
      statement.execute();
    }
  }

  public void update(Connection connection, Way way) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
      statement.setInt(1, way.getInfo().getVersion());
      statement.setInt(2, way.getInfo().getUserId());
      statement.setTimestamp(3, new Timestamp(way.getInfo().getTimestamp()));
      statement.setLong(4, way.getInfo().getChangeset());
      statement.setObject(5, way.getInfo().getTags());
      statement.setObject(6, way.getNodes().toArray(new Long[0]));
      statement.setLong(7, way.getInfo().getId());
      statement.execute();
    }
  }

  public Way select(Connection connection, Long id) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(SELECT)) {
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
  }

  public void delete(Connection connection, Long id) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, id);
      statement.execute();
    }
  }

  public static void copy(PGConnection connection, List<Way> ways) throws Exception {
    try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(connection, COPY_WAYS))) {
      writer.writeHeader();
      for (Way way : ways) {
        writer.startRow(7);
        writer.writeLong(way.getInfo().getId());
        writer.writeInteger(way.getInfo().getVersion());
        writer.writeInteger(way.getInfo().getUserId());
        writer.writeLong(way.getInfo().getTimestamp());
        writer.writeLong(way.getInfo().getChangeset());
        writer.writeHstore(way.getInfo().getTags());
        writer.writeLongList(way.getNodes());
      }
    }
  }

}
