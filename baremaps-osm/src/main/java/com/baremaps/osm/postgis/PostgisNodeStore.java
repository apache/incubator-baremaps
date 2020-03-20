/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.osm.postgis;

import com.baremaps.osm.geometry.GeometryUtil;
import com.baremaps.osm.geometry.NodeBuilder;
import com.baremaps.osm.store.Store;
import com.baremaps.osm.store.StoreException;
import com.baremaps.core.postgis.CopyWriter;
import com.baremaps.osm.model.Info;
import com.baremaps.osm.model.Node;
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
import org.locationtech.jts.geom.Point;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgisNodeStore implements Store<Long, Node> {

  private static final String SELECT =
      "SELECT id, version, uid, timestamp, changeset, tags, st_asbinary(ST_Transform(geom, 4326)) FROM osm_nodes WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, version, uid, timestamp, changeset, tags, st_asbinary(ST_Transform(geom, 4326)) FROM osm_nodes WHERE id = ANY (?)";

  private static final String INSERT =
      "INSERT INTO osm_nodes (id, version, uid, timestamp, changeset, tags, geom) VALUES (?, ?, ?, ?, ?, ?, ?)"
          + "ON CONFLICT (id) DO UPDATE SET "
          + "version = excluded.version, "
          + "uid = excluded.uid, "
          + "timestamp = excluded.timestamp, "
          + "changeset = excluded.changeset, "
          + "tags = excluded.tags, "
          + "geom = excluded.geom";

  private static final String DELETE = "DELETE FROM osm_nodes WHERE id = ?";

  private static final String COPY =
      "COPY osm_nodes (id, version, uid, timestamp, changeset, tags, geom) FROM STDIN BINARY";

  private final DataSource dataSource;

  private final NodeBuilder nodeBuilder;

  public PostgisNodeStore(DataSource dataSource, NodeBuilder nodeBuilder) {
    this.dataSource = dataSource;
    this.nodeBuilder = nodeBuilder;
  }

  public Node get(Long key) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, key);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        long id = result.getLong(1);
        int version = result.getInt(2);
        int uid = result.getInt(3);
        LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
        long changeset = result.getLong(5);
        Map<String, String> tags = (Map<String, String>) result.getObject(6);
        Point point = (Point) GeometryUtil.deserialize(result.getBytes(7));
        return new Node(new Info(id, version, timestamp, changeset, uid, tags), point.getX(), point.getY());
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public List<Node> getAll(List<Long> keys) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Node> nodes = new HashMap<>();
      while (result.next()) {
        long id = result.getLong(1);
        int version = result.getInt(2);
        int uid = result.getInt(3);
        LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
        long changeset = result.getLong(5);
        Map<String, String> tags = (Map<String, String>) result.getObject(6);
        Point point = (Point) GeometryUtil.deserialize(result.getBytes(7));
        nodes.put(
            id,
            new Node(
                new Info(id, version, timestamp, changeset, uid, tags),
                point.getX(),
                point.getY()));
      }
      return keys.stream().map(key -> nodes.get(key)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(Long key, Node value) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, key);
      statement.setInt(2, value.getInfo().getVersion());
      statement.setInt(3, value.getInfo().getUserId());
      statement.setObject(4, value.getInfo().getTimestamp());
      statement.setLong(5, value.getInfo().getChangeset());
      statement.setObject(6, value.getInfo().getTags());
      byte[] wkb = nodeBuilder != null ? GeometryUtil.serialize(nodeBuilder.build(value)) : null;
      statement.setBytes(7, wkb);
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void putAll(List<Entry<Long, Node>> entries) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      for (Entry<Long, Node> entry : entries) {
        Long key = entry.key();
        Node value = entry.value();
        statement.clearParameters();
        statement.setLong(1, key);
        statement.setInt(2, value.getInfo().getVersion());
        statement.setInt(3, value.getInfo().getUserId());
        statement.setObject(4, value.getInfo().getTimestamp());
        statement.setLong(5, value.getInfo().getChangeset());
        statement.setObject(6, value.getInfo().getTags());
        byte[] wkb = nodeBuilder != null ? GeometryUtil.serialize(nodeBuilder.build(value)) : null;
        statement.setBytes(7, wkb);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void delete(Long key) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, key);
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void deleteAll(List<Long> keys) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      for (Long key : keys) {
        statement.clearParameters();
        statement.setLong(1, key);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void importAll(List<Entry<Long, Node>> entries) {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
        writer.writeHeader();
        for (Entry<Long, Node> entry : entries) {
          Long id = entry.key();
          Node node = entry.value();
          writer.startRow(7);
          writer.writeLong(id);
          writer.writeInteger(node.getInfo().getVersion());
          writer.writeInteger(node.getInfo().getUserId());
          writer.writeLocalDateTime(node.getInfo().getTimestamp());
          writer.writeLong(node.getInfo().getChangeset());
          writer.writeHstore(node.getInfo().getTags());
          writer.writeGeometry(nodeBuilder.build(node));
        }
      }
    } catch (Exception e) {
      throw new StoreException(e);
    }
  }
}
