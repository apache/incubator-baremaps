/*
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

package org.apache.baremaps.database.repository;



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
import javax.sql.DataSource;
import org.apache.baremaps.database.copy.CopyWriter;
import org.apache.baremaps.openstreetmap.model.Header;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

/** Provides an implementation of the {@code HeaderRepository} baked by a PostgreSQL table. */
public class PostgresHeaderRepository implements HeaderRepository {

  private final DataSource dataSource;

  private final String createTable;

  private final String dropTable;

  private final String truncateTable;

  private final String selectLatest;

  private final String select;

  private final String selectIn;

  private final String insert;

  private final String delete;

  private final String copy;

  /**
   * Constructs a {@code PostgresHeaderRepository}.
   *
   * @param dataSource
   */
  public PostgresHeaderRepository(DataSource dataSource) {
    this(dataSource, "osm_headers", "replication_sequence_number", "replication_timestamp",
        "replication_url", "source", "writing_program");
  }

  /**
   * Constructs a {@code PostgresHeaderRepository} with custom parameters.
   *
   * @param dataSource
   * @param tableName
   * @param replicationSequenceNumberColumn
   * @param replicationTimestampColumn
   * @param replicationUrlColumn
   * @param sourceColumn
   * @param writingProgramColumn
   */
  public PostgresHeaderRepository(DataSource dataSource, String tableName,
      String replicationSequenceNumberColumn, String replicationTimestampColumn,
      String replicationUrlColumn, String sourceColumn, String writingProgramColumn) {
    this.dataSource = dataSource;
    this.createTable = String.format("""
        CREATE TABLE IF NOT EXISTS %1$s (
          %2$s bigint PRIMARY KEY,
          %3$s timestamp without time zone,
          %4$s text,
          %5$s text,
          %6$s text
        )""", tableName, replicationSequenceNumberColumn, replicationTimestampColumn,
        replicationUrlColumn, sourceColumn, writingProgramColumn);
    this.dropTable = String.format("DROP TABLE IF EXISTS %1$s CASCADE", tableName);
    this.truncateTable = String.format("TRUNCATE TABLE %1$s", tableName);
    this.selectLatest =
        String.format("SELECT %2$s, %3$s, %4$s, %5$s, %6$s FROM %1$s ORDER BY %2$s DESC", tableName,
            replicationSequenceNumberColumn, replicationTimestampColumn, replicationUrlColumn,
            sourceColumn, writingProgramColumn);
    this.select = String.format("SELECT %2$s, %3$s, %4$s, %5$s, %6$s FROM %1$s WHERE %2$s = ?",
        tableName, replicationSequenceNumberColumn, replicationTimestampColumn,
        replicationUrlColumn, sourceColumn, writingProgramColumn);
    this.selectIn =
        String.format("SELECT %2$s, %3$s, %4$s, %5$s, %6$s FROM %1$s WHERE %2$s = ANY (?)",
            tableName, replicationSequenceNumberColumn, replicationTimestampColumn,
            replicationUrlColumn, sourceColumn, writingProgramColumn);
    this.insert = String.format("""
        INSERT INTO %1$s (%2$s, %3$s, %4$s, %5$s, %6$s)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT (%2$s) DO UPDATE SET
        %3$s = excluded.%3$s,
        %4$s = excluded.%4$s,
        %5$s = excluded.%5$s,
        %6$s = excluded.%6$s""", tableName, replicationSequenceNumberColumn,
        replicationTimestampColumn, replicationUrlColumn, sourceColumn, writingProgramColumn);
    this.delete = String.format("DELETE FROM %1$s WHERE %2$s = ?", tableName,
        replicationSequenceNumberColumn);
    this.copy = String.format("COPY %1$s (%2$s, %3$s, %4$s, %5$s, %6$s) FROM STDIN BINARY",
        tableName, replicationSequenceNumberColumn, replicationTimestampColumn,
        replicationUrlColumn, sourceColumn, writingProgramColumn);
  }

  /** {@inheritDoc} */
  @Override
  public void create() throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(createTable)) {
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void drop() throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(dropTable)) {
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void truncate() throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(truncateTable)) {
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Header> selectAll() throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(selectLatest)) {
      try (ResultSet result = statement.executeQuery()) {
        List<Header> values = new ArrayList<>();
        while (result.next()) {
          Header value = getValue(result);
          values.add(value);
        }
        return values;
      }
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Header selectLatest() throws RepositoryException {
    return selectAll().get(0);
  }

  /** {@inheritDoc} */
  @Override
  public Header get(Long key) throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(select)) {
      statement.setObject(1, key);
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          return getValue(result);
        } else {
          return null;
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Header> get(List<Long> keys) throws RepositoryException {
    if (keys.isEmpty()) {
      return List.of();
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(selectIn)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      try (ResultSet result = statement.executeQuery()) {
        Map<Long, Header> values = new HashMap<>();
        while (result.next()) {
          Header value = getValue(result);
          values.put(value.getReplicationSequenceNumber(), value);
        }
        return keys.stream().map(values::get).toList();
      }
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void put(Header value) throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      setValue(statement, value);
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void put(List<Header> values) throws RepositoryException {
    if (values.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      for (Header value : values) {
        statement.clearParameters();
        setValue(statement, value);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void delete(Long key) throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(delete)) {
      statement.setObject(1, key);
      statement.execute();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void delete(List<Long> keys) throws RepositoryException {
    if (keys.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(delete)) {
      for (Long key : keys) {
        statement.clearParameters();
        statement.setObject(1, key);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void copy(List<Header> values) throws RepositoryException {
    if (values.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copy))) {
        writer.writeHeader();
        for (Header value : values) {
          writer.startRow(5);
          writer.writeLong(value.getReplicationSequenceNumber());
          writer.writeLocalDateTime(value.getReplicationTimestamp());
          writer.write(value.getReplicationUrl());
          writer.write(value.getSource());
          writer.write(value.getWritingProgram());
        }
      }
    } catch (IOException | SQLException e) {
      throw new RepositoryException(e);
    }
  }

  private Header getValue(ResultSet resultSet) throws SQLException {
    long replicationSequenceNumber = resultSet.getLong(1);
    LocalDateTime replicationTimestamp = resultSet.getObject(2, LocalDateTime.class);
    String replicationUrl = resultSet.getString(3);
    String source = resultSet.getString(4);
    String writingProgram = resultSet.getString(5);
    return new Header(replicationSequenceNumber, replicationTimestamp, replicationUrl, source,
        writingProgram);
  }

  private void setValue(PreparedStatement statement, Header value) throws SQLException {
    statement.setObject(1, value.getReplicationSequenceNumber());
    statement.setObject(2, value.getReplicationTimestamp());
    statement.setObject(3, value.getReplicationUrl());
    statement.setObject(4, value.getSource());
    statement.setObject(5, value.getWritingProgram());
  }
}
