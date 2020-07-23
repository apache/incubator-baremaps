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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgisRelationStore implements Store<RelationEntity> {

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, st_asbinary(geom) FROM osm_relations WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, st_asbinary(geom) FROM osm_relations WHERE id = ANY (?)";

  private static final String INSERT =
      "INSERT INTO osm_relations (id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, geom) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
          + "ON CONFLICT (id) DO UPDATE SET "
          + "version = EXCLUDED.version, "
          + "uid = EXCLUDED.uid, "
          + "timestamp = EXCLUDED.timestamp, "
          + "changeset = EXCLUDED.changeset, "
          + "tags = EXCLUDED.tags, "
          + "member_refs = EXCLUDED.member_refs, "
          + "member_types = EXCLUDED.member_types, "
          + "member_roles = EXCLUDED.member_roles, "
          + "geom = EXCLUDED.geom";

  private static final String DELETE =
      "DELETE FROM osm_relations WHERE id = ?";

  private static final String COPY =
      "COPY osm_relations (id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, geom) FROM STDIN BINARY";

  private final DataSource dataSource;

  public PostgisRelationStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public RelationEntity get(Long id) {
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
        Long[] refs = (Long[]) result.getArray(6).getArray();
        String[] types = (String[]) result.getArray(7).getArray();
        String[] roles = (String[]) result.getArray(8).getArray();
        Geometry geometry = GeometryUtil.deserialize(result.getBytes(9));
        return new RelationEntity(id, version, timestamp, changeset, uid, tags, refs, types, roles,
            geometry);
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public List<RelationEntity> get(List<Long> ids) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", ids.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, RelationEntity> relations = new HashMap<>();
      while (result.next()) {
        long id = result.getLong(1);
        int version = result.getInt(2);
        int uid = result.getInt(3);
        LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
        long changeset = result.getLong(5);
        Map<String, String> tags = (Map<String, String>) result.getObject(6);
        Long[] refs = (Long[]) result.getArray(7).getArray();
        String[] types = (String[]) result.getArray(8).getArray();
        String[] roles = (String[]) result.getArray(9).getArray();
        Geometry geometry = GeometryUtil.deserialize(result.getBytes(10));
        relations.put(id,
            new RelationEntity(id, version, timestamp, changeset, uid, tags, refs, types, roles, geometry));
      }
      return ids.stream().map(id -> relations.get(id)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(RelationEntity entity) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, entity.getId());
      statement.setInt(2, entity.getVersion());
      statement.setInt(3, entity.getUserId());
      statement.setObject(4, entity.getTimestamp());
      statement.setLong(5, entity.getChangeset());
      statement.setObject(6, entity.getTags());
      statement.setObject(7, connection.createArrayOf("bigint", entity.getMemberRefs()));
      statement.setObject(8, entity.getMemberTypes());
      statement.setObject(9, entity.getMemberRoles());
      statement.setBytes(10, GeometryUtil.serialize(entity.getGeometry()));
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void put(List<RelationEntity> entities) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      for (RelationEntity row : entities) {
        statement.clearParameters();
        statement.setLong(1, row.getId());
        statement.setInt(2, row.getVersion());
        statement.setInt(3, row.getUserId());
        statement.setObject(4, row.getTimestamp());
        statement.setLong(5, row.getChangeset());
        statement.setObject(6, row.getTags());
        statement.setObject(7, connection.createArrayOf("bigint", row.getMemberRefs()));
        statement.setObject(8, row.getMemberTypes());
        statement.setObject(9, row.getMemberRoles());
        statement.setBytes(10, GeometryUtil.serialize(row.getGeometry()));
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
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void copy(List<RelationEntity> entities) {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
        writer.writeHeader();
        for (RelationEntity row : entities) {
          writer.startRow(10);
          writer.writeLong(row.getId());
          writer.writeInteger(row.getVersion());
          writer.writeInteger(row.getUserId());
          writer.writeLocalDateTime(row.getTimestamp());
          writer.writeLong(row.getChangeset());
          writer.writeHstore(row.getTags());
          writer.writeLongList(Arrays.asList(row.getMemberRefs()));
          writer.writeStringList(Arrays.asList(row.getMemberTypes()));
          writer.writeStringList(Arrays.asList(row.getMemberRoles()));
          writer.writeGeometry(row.getGeometry());
        }
      }
    } catch (IOException | SQLException ex) {
      throw new StoreException(ex);
    }
  }

}
