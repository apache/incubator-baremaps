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
import com.baremaps.osm.database.WayTable;
import com.baremaps.osm.domain.Info;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.geometry.GeometryUtils;
import com.baremaps.postgres.jdbc.CopyWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
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
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgresWayTable implements WayTable {

  private final DataSource dataSource;

  private final String select;

  private final String selectIn;

  private final String insert;

  private final String delete;

  private final String copy;

  public PostgresWayTable(DataSource dataSource) {
    this(
        dataSource,
        "osm_ways",
        "id",
        "version",
        "uid",
        "timestamp",
        "changeset",
        "tags",
        "nodes",
        "geom");
  }

  public PostgresWayTable(
      DataSource dataSource,
      String wayTable,
      String idColumn,
      String versionColumn,
      String uidColumn,
      String timestampColumn,
      String changesetColumn,
      String tagsColumn,
      String nodesColumn,
      String geometryColumn) {
    this.dataSource = dataSource;
    this.select =
        String.format(
            "SELECT %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, st_asbinary(%9$s) FROM %1$s WHERE %2$s = ?",
            wayTable,
            idColumn,
            versionColumn,
            uidColumn,
            timestampColumn,
            changesetColumn,
            tagsColumn,
            nodesColumn,
            geometryColumn);
    this.selectIn =
        String.format(
            "SELECT %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, st_asbinary(%9$s) FROM %1$s WHERE %2$s = ANY (?)",
            wayTable,
            idColumn,
            versionColumn,
            uidColumn,
            timestampColumn,
            changesetColumn,
            tagsColumn,
            nodesColumn,
            geometryColumn);
    this.insert =
        String.format(
            "INSERT INTO %1$s (%2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                + "ON CONFLICT (%2$s) DO UPDATE SET "
                + "%3$s = excluded.%3$s, "
                + "%4$s = excluded.%4$s, "
                + "%5$s = excluded.%5$s, "
                + "%6$s = excluded.%6$s, "
                + "%7$s = excluded.%7$s, "
                + "%8$s = excluded.%8$s, "
                + "%9$s = excluded.%9$s",
            wayTable,
            idColumn,
            versionColumn,
            uidColumn,
            timestampColumn,
            changesetColumn,
            tagsColumn,
            nodesColumn,
            geometryColumn);
    this.delete = String.format("DELETE FROM %1$s WHERE %2$s = ?", wayTable, idColumn);
    this.copy =
        String.format(
            "COPY %1$s (%2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s) FROM STDIN BINARY",
            wayTable,
            idColumn,
            versionColumn,
            uidColumn,
            timestampColumn,
            changesetColumn,
            tagsColumn,
            nodesColumn,
            geometryColumn);
  }

  public Way select(Long id) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(select)) {
      statement.setObject(1, id);
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          return getEntity(result);
        } else {
          return null;
        }
      }
    } catch (SQLException | JsonProcessingException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<Way> select(List<Long> ids) throws DatabaseException {
    if (ids.isEmpty()) {
      return List.of();
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(selectIn)) {
      statement.setArray(1, connection.createArrayOf("int8", ids.toArray()));
      try (ResultSet result = statement.executeQuery()) {
        Map<Long, Way> entities = new HashMap<>();
        while (result.next()) {
          Way entity = getEntity(result);
          entities.put(entity.getId(), entity);
        }
        return ids.stream().map(entities::get).collect(Collectors.toList());
      }
    } catch (SQLException | JsonProcessingException e) {
      throw new DatabaseException(e);
    }
  }

  public void insert(Way entity) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      setEntity(statement, entity);
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void insert(List<Way> entities) throws DatabaseException {
    if (entities.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      for (Way entity : entities) {
        statement.clearParameters();
        setEntity(statement, entity);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

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
        statement.execute();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  public void copy(List<Way> entities) throws DatabaseException {
    if (entities.isEmpty()) {
      return;
    }
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copy))) {
        writer.writeHeader();
        for (Way entity : entities) {
          writer.startRow(8);
          writer.writeLong(entity.getId());
          writer.writeInteger(entity.getInfo().getVersion());
          writer.writeInteger(entity.getInfo().getUid());
          writer.writeLocalDateTime(entity.getInfo().getTimestamp());
          writer.writeLong(entity.getInfo().getChangeset());
          writer.writeJsonb(entity.getTags());
          writer.writeLongList(entity.getNodes());
          writer.writeGeometry(entity.getGeometry());
        }
      }
    } catch (IOException | SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private Way getEntity(ResultSet result) throws SQLException, JsonProcessingException {
    long id = result.getLong(1);
    int version = result.getInt(2);
    int uid = result.getInt(3);
    LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
    long changeset = result.getLong(5);
    Map<String, String> tags = PostgresJsonbMapper.convert(result.getString(6));
    List<Long> nodes = new ArrayList<>();
    Array array = result.getArray(7);
    if (array != null) {
      nodes = Arrays.asList((Long[]) array.getArray());
    }
    Geometry geometry = GeometryUtils.deserialize(result.getBytes(8));
    Info info = new Info(version, timestamp, changeset, uid);
    return new Way(id, info, tags, nodes, geometry);
  }

  private void setEntity(PreparedStatement statement, Way entity) throws SQLException {
    statement.setObject(1, entity.getId());
    statement.setObject(2, entity.getInfo().getVersion());
    statement.setObject(3, entity.getInfo().getUid());
    statement.setObject(4, entity.getInfo().getTimestamp());
    statement.setObject(5, entity.getInfo().getChangeset());
    statement.setObject(6, entity.getTags());
    statement.setObject(7, entity.getNodes().stream().mapToLong(Long::longValue).toArray());
    statement.setBytes(8, GeometryUtils.serialize(entity.getGeometry()));
  }
}
