/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.osm.database;

import com.baremaps.osm.domain.Header;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class HeaderTable implements EntityTable<Header> {

  private static final String SELECT =
      "SELECT replication_timestamp, replication_sequence_number, replication_url, source, writing_program FROM osm_headers ORDER BY replication_timestamp DESC";

  private static final String INSERT =
      "INSERT INTO osm_headers (replication_timestamp, replication_sequence_number, replication_url, source, writing_program) VALUES (?, ?, ?, ?, ?)";

  private final DataSource dataSource;

  public HeaderTable(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public List<Header> select() throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
      ResultSet result = statement.executeQuery();
      List<Header> headers = new ArrayList<>();
      while (result.next()) {
        LocalDateTime replicationTimestamp = result.getObject(1, LocalDateTime.class);
        long replicationSequenceNumber = result.getLong(2);
        String replicationUrl = result.getString(3);
        String source = result.getString(4);
        String writingProgram = result.getString(5);
        headers.add(new Header(
            replicationTimestamp,
            replicationSequenceNumber,
            replicationUrl,
            source,
            writingProgram));
      }
      return headers;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  public Header latest() throws DatabaseException {
    return select().get(0);
  }

  @Override
  public Header select(Long id) throws DatabaseException {
    return null;
  }

  @Override
  public List<Header> select(List<Long> ids) throws DatabaseException {
    return null;
  }

  public void insert(Header header) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setObject(1, header.getReplicationTimestamp());
      statement.setObject(2, header.getReplicationSequenceNumber());
      statement.setString(3, header.getReplicationUrl());
      statement.setString(4, header.getSource());
      statement.setString(5, header.getWritingProgram());
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void insert(List<Header> elements) throws DatabaseException {
    // TODO: implement this method
  }

  @Override
  public void delete(Long id) throws DatabaseException {
    // TODO: implement this method
  }

  @Override
  public void delete(List<Long> ids) throws DatabaseException {
    // TODO: implement this method
  }

  @Override
  public void copy(List<Header> elemenets) throws DatabaseException {
    // TODO: implement this method
  }

}
