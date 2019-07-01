package io.gazetteer.osm.database;

import io.gazetteer.osm.model.Header;
import io.gazetteer.osm.util.GeometryUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

public class HeaderTable {

  private static final String SELECT =
      "SELECT replication_timestamp, replication_sequence_number, replication_url, source, writing_program, bbox FROM osm_header ORDER BY replication_timestamp DESC";

  private static final String INSERT =
      "INSERT INTO osm_header (replication_timestamp, replication_sequence_number, replication_url, source, writing_program, bbox) VALUES (?, ?, ?, ?, ?, ?)";


  public static List<Header> select(Connection connection) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(SELECT)) {
      ResultSet result = statement.executeQuery();
      List<Header> headers = new ArrayList<>();
      while (result.next()) {
        long replicationTimestamp = result.getLong(1);
        long replicationSequenceNumber = result.getLong(2);
        String replicationUrl = result.getString(3);
        String source = result.getString(4);
        String writingProgram = result.getString(5);
        Geometry bbox = GeometryUtil.asGeometry(result.getBytes(6));
        headers.add(new Header(replicationTimestamp, replicationSequenceNumber, replicationUrl, source, writingProgram, bbox));
      }
      return headers;
    }
  }

  public static void insert(Connection connection, Header header) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, header.getReplicationTimestamp());
      statement.setLong(2, header.getReplicationSequenceNumber());
      statement.setString(3, header.getReplicationUrl());
      statement.setString(4, header.getSource());
      statement.setString(5, header.getWritingProgram());
      statement.setBytes(6, GeometryUtil.asWKB(header.getBbox()));
      statement.execute();
    }
  }

}
