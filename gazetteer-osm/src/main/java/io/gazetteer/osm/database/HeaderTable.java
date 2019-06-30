package io.gazetteer.osm.database;

import io.gazetteer.osm.model.Header;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HeaderTable {

  private static final String SELECT =
      "SELECT replication_timestamp, replication_sequence_number, replication_url, bbox FROM osm_header ORDER BY replication_timestamp DESC";

  private static final String INSERT =
      "INSERT INTO osm_header (replication_timestamp, replication_sequence_number, replication_url, bbox) VALUES (?, ?, ?, ?)";


  public static List<Header> select(Connection connection) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(SELECT)) {
      ResultSet result = statement.executeQuery();
      List<Header> headers = new ArrayList<>();
      while (result.next()) {
        long replicationTimestamp = result.getLong(1);
        long replicationSequenceNumber = result.getLong(2);
        String replicationUrl = result.getString(3);
        String bbox = result.getString(4);
        headers.add(new Header(replicationTimestamp, replicationSequenceNumber, replicationUrl, bbox));
      }
      return headers;
    }
  }

  public static void insert(Connection connection, Header header) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, header.getReplicationTimestamp());
      statement.setLong(2, header.getReplicationSequenceNumber());
      statement.setString(3, header.getReplicationUrl());
      statement.setString(4, header.getBbox());
      statement.execute();
    }
  }

}
