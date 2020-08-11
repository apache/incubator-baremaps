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
import com.baremaps.osm.model.Member;
import com.baremaps.osm.model.Relation;
import com.baremaps.util.postgis.CopyWriter;
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
import javax.inject.Inject;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgisRelationStore implements Store<Relation> {

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

  @Inject
  public PostgisRelationStore(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Relation get(Long id) throws StoreException {
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
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < refs.length; i++) {
          members.add(new Member(refs[i], types[i], roles[i]));
        }
        Geometry geometry = GeometryUtil.deserialize(result.getBytes(9));
        return new Relation(id, version, timestamp, changeset, uid, tags, members, geometry);
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public List<Relation> get(List<Long> ids) throws StoreException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", ids.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Relation> relations = new HashMap<>();
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
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < refs.length; i++) {
          members.add(new Member(refs[i], types[i], roles[i]));
        }
        Geometry geometry = GeometryUtil.deserialize(result.getBytes(10));
        relations.put(id, new Relation(id, version, timestamp, changeset, uid, tags, members, geometry));
      }
      return ids.stream().map(id -> relations.get(id)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(Relation entity) throws StoreException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, entity.getId());
      statement.setInt(2, entity.getVersion());
      statement.setInt(3, entity.getUserId());
      statement.setObject(4, entity.getTimestamp());
      statement.setLong(5, entity.getChangeset());
      statement.setObject(6, entity.getTags());
      Object[] refs = entity.getMembers().stream().map(Member::getRef).toArray();
      statement.setObject(7, connection.createArrayOf("bigint", refs));
      Object[] types = entity.getMembers().stream().map(Member::getType).toArray();
      statement.setObject(8, connection.createArrayOf("varchar", types));
      Object[] roles = entity.getMembers().stream().map(Member::getRole).toArray();
      statement.setObject(9,  connection.createArrayOf("varchar", roles));
      statement.setBytes(10, GeometryUtil.serialize(entity.getGeometry().orElse(null)));
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void put(List<Relation> entities) throws StoreException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      for (Relation entity : entities) {
        statement.clearParameters();
        statement.setLong(1, entity.getId());
        statement.setInt(2, entity.getVersion());
        statement.setInt(3, entity.getUserId());
        statement.setObject(4, entity.getTimestamp());
        statement.setLong(5, entity.getChangeset());
        statement.setObject(6, entity.getTags());
        Object[] refs = entity.getMembers().stream().map(Member::getRef).toArray();
        statement.setObject(7, connection.createArrayOf("bigint", refs));
        Object[] types = entity.getMembers().stream().map(Member::getType).toArray();
        statement.setObject(8, connection.createArrayOf("varchar", types));
        Object[] roles = entity.getMembers().stream().map(Member::getRole).toArray();
        statement.setObject(9,  connection.createArrayOf("varchar", roles));
        statement.setBytes(10, GeometryUtil.serialize(entity.getGeometry().orElse(null)));
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void delete(Long id) throws StoreException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, id);
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void delete(List<Long> ids) throws StoreException {
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

  public void copy(List<Relation> entities) throws StoreException {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
        writer.writeHeader();
        for (Relation entity : entities) {
          writer.startRow(10);
          writer.writeLong(entity.getId());
          writer.writeInteger(entity.getVersion());
          writer.writeInteger(entity.getUserId());
          writer.writeLocalDateTime(entity.getTimestamp());
          writer.writeLong(entity.getChangeset());
          writer.writeHstore(entity.getTags());
          writer.writeLongList(entity.getMembers().stream().map(Member::getRef).collect(Collectors.toList()));
          writer.writeStringList(entity.getMembers().stream().map(Member::getType).collect(Collectors.toList()));
          writer.writeStringList(entity.getMembers().stream().map(Member::getRole).collect(Collectors.toList()));
          writer.writeGeometry(entity.getGeometry().orElse(null));
        }
      }
    } catch (IOException | SQLException ex) {
      throw new StoreException(ex);
    }
  }

}
