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

package com.baremaps.importer.database;

import com.baremaps.osm.geometry.GeometryUtil;
import com.baremaps.osm.model.Node;
import com.baremaps.util.postgis.CopyWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class NodeTable implements Table<Node> {

  private final DataSource dataSource;

  private final String select;

  private final String selectIn;

  private final String insert;

  private final String delete;

  private final String copy;

  public NodeTable(DataSource dataSource) {
    this(dataSource, "osm_nodes", "id", "version", "uid", "timestamp", "changeset", "tags", "lon", "lat", "geom");
  }

  @Inject
  public NodeTable(DataSource dataSource, String nodeTable, String idColumn, String versionColumn, String uidColumn,
      String timestampColumn, String changesetColumn, String tagsColumn, String longitudeColumn,
      String latitudeColumn, String geometryColumn) {
    this.dataSource = dataSource;
    this.select = String.format(
        "SELECT %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, st_asbinary(%10$s) FROM %1$s WHERE %2$s = ?",
        nodeTable, idColumn, versionColumn, uidColumn, timestampColumn,
        changesetColumn, tagsColumn, longitudeColumn, latitudeColumn, geometryColumn);
    this.selectIn = String.format(
        "SELECT %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, st_asbinary(%10$s) FROM %1$s WHERE %2$s = ANY (?)",
        nodeTable, idColumn, versionColumn, uidColumn, timestampColumn,
        changesetColumn, tagsColumn, longitudeColumn, latitudeColumn, geometryColumn);
    this.insert = String.format(
        "INSERT INTO %1$s (%2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            + "ON CONFLICT (%2$s) DO UPDATE SET "
            + "%3$s = excluded.%3$s, "
            + "%4$s = excluded.%4$s, "
            + "%5$s = excluded.%5$s, "
            + "%6$s = excluded.%6$s, "
            + "%7$s = excluded.%7$s, "
            + "%8$s = excluded.%8$s, "
            + "%9$s = excluded.%9$s, "
            + "%10$s = excluded.%10$s",
        nodeTable, idColumn, versionColumn, uidColumn, timestampColumn,
        changesetColumn, tagsColumn, longitudeColumn, latitudeColumn, geometryColumn);
    this.delete = String.format(
        "DELETE FROM %1$s WHERE %2$s = ?",
        nodeTable, idColumn);
    this.copy = String.format(
        "COPY %1$s (%2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s) FROM STDIN BINARY",
        nodeTable, idColumn, versionColumn, uidColumn, timestampColumn,
        changesetColumn, tagsColumn, longitudeColumn, latitudeColumn, geometryColumn);
  }

  public Node select(Long id) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(select)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        return getNode(result);
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  public List<Node> select(List<Long> ids) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(selectIn)) {
      statement.setArray(1, connection.createArrayOf("int8", ids.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Node> nodes = new HashMap<>();
      while (result.next()) {
        Node node = getNode(result);
        nodes.put(node.getId(), node);
      }
      return ids.stream().map(id -> nodes.get(id)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private Node getNode(ResultSet result) throws SQLException {
    long id = result.getLong(1);
    int version = result.getInt(2);
    int uid = result.getInt(3);
    LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
    long changeset = result.getLong(5);
    Map<String, String> tags = (Map<String, String>) result.getObject(6);
    double lon = result.getDouble(7);
    double lat = result.getDouble(8);
    Geometry point = GeometryUtil.deserialize(result.getBytes(9));
    return new Node(id, version, timestamp, changeset, uid, tags, lon, lat, point);
  }

  public void insert(Node entity) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      setNode(statement, entity);
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  public void insert(List<Node> entities) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      for (Node entity : entities) {
        statement.clearParameters();
        setNode(statement, entity);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private void setNode(PreparedStatement statement, Node entity) throws SQLException {
    statement.setLong(1, entity.getId());
    statement.setInt(2, entity.getVersion());
    statement.setInt(3, entity.getUserId());
    statement.setObject(4, entity.getTimestamp());
    statement.setLong(5, entity.getChangeset());
    statement.setObject(6, entity.getTags());
    statement.setDouble(7, entity.getLon());
    statement.setDouble(8, entity.getLat());
    statement.setBytes(9, GeometryUtil.serialize(entity.getGeometry().get()));
  }

  public void delete(Long id) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(delete)) {
      statement.setLong(1, id);
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  public void delete(List<Long> ids) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(delete)) {
      for (Long id : ids) {
        statement.clearParameters();
        statement.setLong(1, id);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  public void copy(List<Node> entities) throws DatabaseException {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copy))) {
        writer.writeHeader();
        for (Node node : entities) {
          writer.startRow(9);
          writer.writeLong(node.getId());
          writer.writeInteger(node.getVersion());
          writer.writeInteger(node.getUserId());
          writer.writeLocalDateTime(node.getTimestamp());
          writer.writeLong(node.getChangeset());
          writer.writeHstore(node.getTags());
          writer.writeDouble(node.getLon());
          writer.writeDouble(node.getLat());
          writer.writeGeometry(node.getGeometry().get());
        }
      }
    } catch (IOException | SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
