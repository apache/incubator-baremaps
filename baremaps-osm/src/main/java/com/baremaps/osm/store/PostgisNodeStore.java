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

package com.baremaps.osm.store;

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
import javax.sql.DataSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgisNodeStore implements Store<Node> {

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, lon, lat, st_asbinary(geom) FROM osm_nodes WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, version, uid, timestamp, changeset, tags, lon, lat, st_asbinary(geom) FROM osm_nodes WHERE id = ANY (?)";

  private static final String INSERT =
      "INSERT INTO osm_nodes (id, version, uid, timestamp, changeset, tags, lon, lat, geom) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
          + "ON CONFLICT (id) DO UPDATE SET "
          + "version = excluded.version, "
          + "uid = excluded.uid, "
          + "timestamp = excluded.timestamp, "
          + "changeset = excluded.changeset, "
          + "tags = excluded.tags, "
          + "lon = excluded.lon, "
          + "lat = excluded.lat, "
          + "geom = excluded.geom";

  private static final String DELETE =
      "DELETE FROM osm_nodes WHERE id = ?";

  private static final String COPY =
      "COPY osm_nodes (id, version, uid, timestamp, changeset, tags, lon, lat, geom) FROM STDIN BINARY";

  private final DataSource dataSource;

  public PostgisNodeStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Node get(Long id) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        int version = result.getInt(1);
        int uid = result.getInt(2);
        LocalDateTime timestamp = result.getObject(3, LocalDateTime.class);
        long changeset = result.getLong(4);
        Map<String, String> tags = (Map<String, String>) result.getObject(5);
        double lon = result.getDouble(6);
        double lat = result.getDouble(7);
        Point point = (Point) GeometryUtil.deserialize(result.getBytes(8));
        return new Node(id, version, timestamp, changeset, uid, tags, lon, lat, point);
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public List<Node> get(List<Long> ids) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", ids.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Node> nodes = new HashMap<>();
      while (result.next()) {
        long id = result.getLong(1);
        int version = result.getInt(2);
        int uid = result.getInt(3);
        LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
        long changeset = result.getLong(5);
        Map<String, String> tags = (Map<String, String>) result.getObject(6);
        double lon = result.getDouble(7);
        double lat = result.getDouble(8);
        Geometry point = GeometryUtil.deserialize(result.getBytes(9));
        nodes.put(id, new Node(id, version, timestamp, changeset, uid, tags, lon, lat, point));
      }
      return ids.stream().map(id -> nodes.get(id)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(Node entity) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, entity.getId());
      statement.setInt(2, entity.getVersion());
      statement.setInt(3, entity.getUserId());
      statement.setObject(4, entity.getTimestamp());
      statement.setLong(5, entity.getChangeset());
      statement.setObject(6, entity.getTags());
      statement.setDouble(7, entity.getLon());
      statement.setDouble(8, entity.getLat());
      statement.setBytes(9, GeometryUtil.serialize(entity.getGeometry().get()));
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(List<Node> entities) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      for (Node entity : entities) {
        statement.clearParameters();
        statement.setLong(1, entity.getId());
        statement.setInt(2, entity.getVersion());
        statement.setInt(3, entity.getUserId());
        statement.setObject(4, entity.getTimestamp());
        statement.setLong(5, entity.getChangeset());
        statement.setObject(6, entity.getTags());
        statement.setDouble(7, entity.getLon());
        statement.setDouble(8, entity.getLat());
        statement.setBytes(9, GeometryUtil.serialize(entity.getGeometry().get()));
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void delete(Long id) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, id);
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void delete(List<Long> ids) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      for (Long id : ids) {
        statement.clearParameters();
        statement.setLong(1, id);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void copy(List<Node> entities) {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
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
      throw new StoreException(e);
    }
  }
}
