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
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.geometry.GeometryUtil;
import com.baremaps.osm.domain.Info;
import com.baremaps.osm.domain.Node;
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
import javax.sql.DataSource;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgresNodeTable implements NodeTable {

  private final DataSource dataSource;

  private final String select;

  private final String selectIn;

  private final String insert;

  private final String delete;

  private final String copy;

  public PostgresNodeTable(DataSource dataSource) {
    this(dataSource,
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

  public PostgresNodeTable(
      DataSource dataSource,
      String nodeTable,
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
      statement.setObject(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        return getNode(result);
      } else {
        return null;
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
    Info info = new Info(version, timestamp, changeset, uid);
    return new Node(id, info, tags, lon, lat, point);
  }

  public void insert(Node element) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      setNode(statement, element);
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  public void insert(List<Node> elements) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      for (Node entity : elements) {
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
    statement.setObject(1, entity.getId());
    statement.setObject(2, entity.getInfo().getVersion());
    statement.setObject(3, entity.getInfo().getUid());
    statement.setObject(4, entity.getInfo().getTimestamp());
    statement.setObject(5, entity.getInfo().getChangeset());
    statement.setObject(6, entity.getTags());
    statement.setObject(7, entity.getLon());
    statement.setObject(8, entity.getLat());
    statement.setBytes(9, GeometryUtil.serialize(entity.getGeometry()));
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

  public void delete(List<Long> ids) throws DatabaseException {
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

  public void copy(List<Node> elements) throws DatabaseException {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copy))) {
        writer.writeHeader();
        for (Node node : elements) {
          writer.startRow(9);
          writer.writeLong(node.getId());
          writer.writeInteger(node.getInfo().getVersion());
          writer.writeInteger(node.getInfo().getUid());
          writer.writeLocalDateTime(node.getInfo().getTimestamp());
          writer.writeLong(node.getInfo().getChangeset());
          writer.writeHstore(node.getTags());
          writer.writeDouble(node.getLon());
          writer.writeDouble(node.getLat());
          writer.writeGeometry(node.getGeometry());
        }
      }
    } catch (IOException | SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
