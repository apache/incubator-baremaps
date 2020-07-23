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
import com.baremaps.util.postgis.CopyWriter;
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

public class PostgisWayStore implements Store<WayEntity> {

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, nodes, st_asbinary(geom) FROM osm_ways WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, version, uid, timestamp, changeset, tags, nodes, st_asbinary(geom) FROM osm_ways WHERE id = ANY (?)";

  private static final String INSERT =
      "INSERT INTO osm_ways (id, version, uid, timestamp, changeset, tags, nodes, geom) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
          + "ON CONFLICT (id) DO UPDATE SET "
          + "version = excluded.version, "
          + "uid = excluded.uid, "
          + "timestamp = excluded.timestamp, "
          + "changeset = excluded.changeset, "
          + "tags = excluded.tags, "
          + "nodes = excluded.nodes, "
          + "geom = excluded.geom";

  private static final String DELETE = "DELETE FROM osm_ways WHERE id = ?";

  private static final String COPY =
      "COPY osm_ways (id, version, uid, timestamp, changeset, tags, nodes, geom) FROM STDIN BINARY";

  private final DataSource dataSource;

  public PostgisWayStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public WayEntity get(Long id) {
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
        List<Long> nodes = new ArrayList<>();
        Array array = result.getArray(6);
        if (array != null) {
          nodes = Arrays.asList((Long[]) array.getArray());
        }
        Geometry geometry = GeometryUtil.deserialize(result.getBytes(7));
        return new WayEntity(id, version, timestamp, changeset, uid, tags, nodes, geometry);
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public List<WayEntity> get(List<Long> ids) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", ids.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, WayEntity> ways = new HashMap<>();
      while (result.next()) {
        long id = result.getLong(1);
        int version = result.getInt(2);
        int uid = result.getInt(3);
        LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
        long changeset = result.getLong(5);
        Map<String, String> tags = (Map<String, String>) result.getObject(6);
        List<Long> nodes = new ArrayList<>();
        Array array = result.getArray(7);
        if (array != null) {
          nodes = Arrays.asList((Long[]) array.getArray());
        }
        Geometry geometry = GeometryUtil.deserialize(result.getBytes(8));
        ways.put(id, new WayEntity(id, version, timestamp, changeset, uid, tags, nodes, geometry));
      }
      return ids.stream().map(key -> ways.get(key)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(WayEntity entity) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, entity.getId());
      statement.setInt(2, entity.getVersion());
      statement.setInt(3, entity.getUserId());
      statement.setObject(4, entity.getTimestamp());
      statement.setLong(5, entity.getChangeset());
      statement.setObject(6, entity.getTags());
      statement.setObject(7, entity.getNodes().stream().mapToLong(Long::longValue).toArray());
      statement.setBytes(8, GeometryUtil.serialize(entity.getGeometry()));
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void put(List<WayEntity> entities) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      for (WayEntity row : entities) {
        statement.clearParameters();
        statement.setLong(1, row.getId());
        statement.setInt(2, row.getVersion());
        statement.setInt(3, row.getUserId());
        statement.setObject(4, row.getTimestamp());
        statement.setLong(5, row.getChangeset());
        statement.setObject(6, row.getTags());
        statement.setObject(7, row.getNodes().stream().mapToLong(Long::longValue).toArray());
        statement.setBytes(8, GeometryUtil.serialize(row.getGeometry()));
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

  @Override
  public void delete(List<Long> ids) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      for (Long id : ids) {
        statement.clearParameters();
        statement.setLong(1, id);
        statement.execute();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void copy(List<WayEntity> entities) {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
        writer.writeHeader();
        for (WayEntity row : entities) {
          writer.startRow(8);
          writer.writeLong(row.getId());
          writer.writeInteger(row.getVersion());
          writer.writeInteger(row.getUserId());
          writer.writeLocalDateTime(row.getTimestamp());
          writer.writeLong(row.getChangeset());
          writer.writeHstore(row.getTags());
          writer.writeLongList(row.getNodes());
          writer.writeGeometry(row.getGeometry());
        }
      }
    } catch (IOException | SQLException ex) {
      throw new StoreException(ex);
    }
  }

}
