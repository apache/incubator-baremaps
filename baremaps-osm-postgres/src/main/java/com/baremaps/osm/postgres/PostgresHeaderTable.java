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

package com.baremaps.osm.postgres;

import com.baremaps.osm.database.DatabaseException;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.domain.Header;
import com.baremaps.postgres.jdbc.CopyWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgresHeaderTable implements HeaderTable {

  private final DataSource dataSource;

  private final String selectLatest;

  private final String select;

  private final String selectIn;

  private final String insert;

  private final String delete;

  private final String copy;

  public PostgresHeaderTable(DataSource dataSource) {
    this(dataSource,
        "osm_headers",
        "replication_sequence_number",
        "replication_timestamp",
        "replication_url",
        "source",
        "writing_program");
  }

  public PostgresHeaderTable(
      DataSource dataSource,
      String headerTable,
      String replicationSequenceNumberColumn,
      String replicationTimestampColumn,
      String replicationUrlColumn,
      String sourceColumn,
      String writingProgramColumn
  ) {
    this.dataSource = dataSource;
    this.selectLatest = String.format(
        "SELECT %2$s, %3$s, %4$s, %5$s, %6$s FROM %1$s ORDER BY %2$s DESC",
        headerTable, replicationSequenceNumberColumn, replicationTimestampColumn,
        replicationUrlColumn, sourceColumn, writingProgramColumn);
    this.select = String.format(
        "SELECT %2$s, %3$s, %4$s, %5$s, %6$s FROM %1$s WHERE %2$s = ?",
        headerTable, replicationSequenceNumberColumn, replicationTimestampColumn,
        replicationUrlColumn, sourceColumn, writingProgramColumn);
    this.selectIn = String.format(
        "SELECT %2$s, %3$s, %4$s, %5$s, %6$s FROM %1$s WHERE %2$s = ANY (?)",
        headerTable, replicationSequenceNumberColumn, replicationTimestampColumn,
        replicationUrlColumn, sourceColumn, writingProgramColumn);
    this.insert = String.format(
        "INSERT INTO %1$s (%2$s, %3$s, %4$s, %5$s, %6$s) "
            + "VALUES (?, ?, ?, ?, ?)"
            + "ON CONFLICT (%2$s) DO UPDATE SET "
            + "%3$s = excluded.%3$s, "
            + "%4$s = excluded.%4$s, "
            + "%5$s = excluded.%5$s, "
            + "%6$s = excluded.%6$s",
        headerTable, replicationSequenceNumberColumn, replicationTimestampColumn,
        replicationUrlColumn, sourceColumn, writingProgramColumn);
    this.delete = String.format(
        "DELETE FROM %1$s WHERE %2$s = ?",
        headerTable, replicationSequenceNumberColumn);
    this.copy = String.format(
        "COPY %1$s (%2$s, %3$s, %4$s, %5$s, %6$s) FROM STDIN BINARY",
        headerTable, replicationSequenceNumberColumn, replicationTimestampColumn,
        replicationUrlColumn, sourceColumn, writingProgramColumn);
  }

  @Override
  public List<Header> selectAll() throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(selectLatest)) {
      ResultSet result = statement.executeQuery();
      List<Header> entities = new ArrayList<>();
      while (result.next()) {
        Header entity = getEntity(result);
        entities.add(entity);
      }
      return entities;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public Header selectLatest() throws DatabaseException {
    return selectAll().get(0);
  }

  @Override
  public Header select(Long id) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(select)) {
      statement.setObject(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        return getEntity(result);
      } else {
        return null;
      }
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<Header> select(List<Long> ids) throws DatabaseException {
    if (ids.isEmpty()) {
      return List.of();
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(selectIn)) {
      statement.setArray(1, connection.createArrayOf("int8", ids.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Header> entities = new HashMap<>();
      while (result.next()) {
        Header entity = getEntity(result);
        entities.put(entity.getReplicationSequenceNumber(), entity);
      }
      return ids.stream().map(id -> entities.get(id)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void insert(Header entity) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      setEntity(statement, entity);
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void insert(List<Header> entities) throws DatabaseException {
    if (entities.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      for (Header entity : entities) {
        statement.clearParameters();
        setEntity(statement, entity);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void delete(Long id) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(delete)) {
      statement.setObject(1, id);
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void delete(List<Long> ids) throws DatabaseException {
    if (ids.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(delete)) {
      for (Long id : ids) {
        statement.clearParameters();
        statement.setObject(1, id);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void copy(List<Header> entities) throws DatabaseException {
    if (entities.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copy))) {
        writer.writeHeader();
        for (Header entity : entities) {
          writer.startRow(5);
          writer.writeLong(entity.getReplicationSequenceNumber());
          writer.writeLocalDateTime(entity.getReplicationTimestamp());
          writer.writeString(entity.getReplicationUrl());
          writer.writeString(entity.getSource());
          writer.writeString(entity.getWritingProgram());
        }
      }
    } catch (IOException | SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private Header getEntity(ResultSet result) throws SQLException {
    long replicationSequenceNumber = result.getLong(1);
    LocalDateTime replicationTimestamp = result.getObject(2, LocalDateTime.class);
    String replicationUrl = result.getString(3);
    String source = result.getString(4);
    String writingProgram = result.getString(5);
    return new Header(
        replicationSequenceNumber,
        replicationTimestamp,
        replicationUrl,
        source,
        writingProgram);
  }

  private void setEntity(PreparedStatement statement, Header entity) throws SQLException {
    statement.setObject(1, entity.getReplicationSequenceNumber());
    statement.setObject(2, entity.getReplicationTimestamp());
    statement.setObject(3, entity.getReplicationUrl());
    statement.setObject(4, entity.getSource());
    statement.setObject(5, entity.getWritingProgram());
  }

}
