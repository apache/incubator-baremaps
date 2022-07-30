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

package com.baremaps.database.repository;

import com.baremaps.osm.geometry.GeometryUtils;
import com.baremaps.osm.model.Info;
import com.baremaps.osm.model.Node;
import com.baremaps.postgres.CopyWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

/** Provides an implementation of the {@code Repository<Node>} baked by PostgreSQL. */
public class PostgresNodeRepository implements Repository<Long, Node> {

  private final DataSource dataSource;

  private final String createTable;

  private final String dropTable;

  private final String truncateTable;

  private final String select;

  private final String selectIn;

  private final String insert;

  private final String delete;

  private final String copy;

  /**
   * Constructs a {@code PostgresNodeRepository}.
   *
   * @param dataSource
   */
  public PostgresNodeRepository(DataSource dataSource) {
    this(
        dataSource,
        "osm_nodes",
        "id",
        "version",
        "uid",
        "timestamp",
        "changeset",
        "tags",
        "lon",
        "lat",
        "geom");
  }

  /**
   * Constructs a {@code PostgresNodeRepository} with custom parameters.
   *
   * @param dataSource
   * @param tableName
   * @param idColumn
   * @param versionColumn
   * @param uidColumn
   * @param timestampColumn
   * @param changesetColumn
   * @param tagsColumn
   * @param longitudeColumn
   * @param latitudeColumn
   * @param geometryColumn
   */
  public PostgresNodeRepository(
      DataSource dataSource,
      String tableName,
      String idColumn,
      String versionColumn,
      String uidColumn,
      String timestampColumn,
      String changesetColumn,
      String tagsColumn,
      String longitudeColumn,
      String latitudeColumn,
      String geometryColumn) {
    this.dataSource = dataSource;
    this.createTable =
        String.format(
            """
                CREATE TABLE %1$s
                (
                    %2$s bigint PRIMARY KEY,
                    %3$s int,
                    %4$s int,
                    %5$s timestamp without time zone,
                    %6$s bigint,
                    %7$s jsonb,
                    %8$s float,
                    %9$s float,
                    %10$s geometry(point)
                )""",
            tableName,
            idColumn,
            versionColumn,
            uidColumn,
            timestampColumn,
            changesetColumn,
            tagsColumn,
            longitudeColumn,
            latitudeColumn,
            geometryColumn);
    this.dropTable = String.format("DROP TABLE IF EXISTS %1$s", tableName);
    this.truncateTable = String.format("TRUNCATE TABLE %1$s", tableName);
    this.select =
        String.format(
            "SELECT %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, st_asbinary(%10$s) FROM %1$s WHERE %2$s = ?",
            tableName,
            idColumn,
            versionColumn,
            uidColumn,
            timestampColumn,
            changesetColumn,
            tagsColumn,
            longitudeColumn,
            latitudeColumn,
            geometryColumn);
    this.selectIn =
        String.format(
            "SELECT %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, st_asbinary(%10$s) FROM %1$s WHERE %2$s = ANY (?)",
            tableName,
            idColumn,
            versionColumn,
            uidColumn,
            timestampColumn,
            changesetColumn,
            tagsColumn,
            longitudeColumn,
            latitudeColumn,
            geometryColumn);
    this.insert =
        String.format(
            "INSERT INTO %1$s (%2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s) "
                + "VALUES (?, ? ,? , ?, ?, cast (? AS jsonb), ?, ?, ?)"
                + "ON CONFLICT (%2$s) DO UPDATE SET "
                + "%3$s = excluded.%3$s, "
                + "%4$s = excluded.%4$s, "
                + "%5$s = excluded.%5$s, "
                + "%6$s = excluded.%6$s, "
                + "%7$s = excluded.%7$s, "
                + "%8$s = excluded.%8$s, "
                + "%9$s = excluded.%9$s, "
                + "%10$s = excluded.%10$s",
            tableName,
            idColumn,
            versionColumn,
            uidColumn,
            timestampColumn,
            changesetColumn,
            tagsColumn,
            longitudeColumn,
            latitudeColumn,
            geometryColumn);
    this.delete = String.format("DELETE FROM %1$s WHERE %2$s = ?", tableName, idColumn);
    this.copy =
        String.format(
            "COPY %1$s (%2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s) FROM STDIN BINARY",
            tableName,
            idColumn,
            versionColumn,
            uidColumn,
            timestampColumn,
            changesetColumn,
            tagsColumn,
            longitudeColumn,
            latitudeColumn,
            geometryColumn);
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
  public Node get(Long key) throws RepositoryException {
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
    } catch (SQLException | JsonProcessingException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Node> get(List<Long> keys) throws RepositoryException {
    if (keys.isEmpty()) {
      return List.of();
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(selectIn)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      try (ResultSet result = statement.executeQuery()) {
        Map<Long, Node> values = new HashMap<>();
        while (result.next()) {
          Node value = getValue(result);
          values.put(value.getId(), value);
        }
        return keys.stream().map(values::get).toList();
      }
    } catch (SQLException | JsonProcessingException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void put(Node value) throws RepositoryException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      setValue(statement, value);
      statement.execute();
    } catch (SQLException | JsonProcessingException e) {
      throw new RepositoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void put(List<Node> values) throws RepositoryException {
    if (values.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      for (Node value : values) {
        statement.clearParameters();
        setValue(statement, value);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException | JsonProcessingException e) {
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
  public void copy(List<Node> values) throws RepositoryException {
    if (values.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copy))) {
        writer.writeHeader();
        for (Node value : values) {
          writer.startRow(9);
          writer.writeLong(value.getId());
          writer.writeInteger(value.getInfo().getVersion());
          writer.writeInteger(value.getInfo().getUid());
          writer.writeLocalDateTime(value.getInfo().getTimestamp());
          writer.writeLong(value.getInfo().getChangeset());
          writer.writeJsonb(PostgresJsonbMapper.toJson(value.getTags()));
          writer.writeDouble(value.getLon());
          writer.writeDouble(value.getLat());
          writer.writePostgisGeometry(value.getGeometry());
        }
      }
    } catch (IOException | SQLException e) {
      throw new RepositoryException(e);
    }
  }

  private Node getValue(ResultSet resultSet) throws SQLException, JsonProcessingException {
    long id = resultSet.getLong(1);
    int version = resultSet.getInt(2);
    int uid = resultSet.getInt(3);
    LocalDateTime timestamp = resultSet.getObject(4, LocalDateTime.class);
    long changeset = resultSet.getLong(5);
    Map<String, String> tags = PostgresJsonbMapper.toMap(resultSet.getString(6));
    double lon = resultSet.getDouble(7);
    double lat = resultSet.getDouble(8);
    Geometry point = GeometryUtils.deserialize(resultSet.getBytes(9));
    Info info = new Info(version, timestamp, changeset, uid);
    return new Node(id, info, tags, lon, lat, point);
  }

  private void setValue(PreparedStatement statement, Node value)
      throws SQLException, JsonProcessingException {
    statement.setObject(1, value.getId());
    statement.setObject(2, value.getInfo().getVersion());
    statement.setObject(3, value.getInfo().getUid());
    statement.setObject(4, value.getInfo().getTimestamp());
    statement.setObject(5, value.getInfo().getChangeset());
    statement.setObject(6, PostgresJsonbMapper.toJson(value.getTags()));
    statement.setObject(7, value.getLon());
    statement.setObject(8, value.getLat());
    statement.setBytes(9, GeometryUtils.serialize(value.getGeometry()));
  }
}
