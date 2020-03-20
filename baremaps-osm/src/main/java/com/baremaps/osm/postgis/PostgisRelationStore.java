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
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.store.Store;
import com.baremaps.osm.store.StoreException;
import com.baremaps.core.postgis.CopyWriter;
import com.baremaps.osm.model.Info;
import com.baremaps.osm.model.Member;
import com.baremaps.osm.model.Member.Type;
import com.baremaps.osm.model.Relation;
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
import javax.sql.DataSource;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class PostgisRelationStore implements Store<Long, Relation> {

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles FROM osm_relations WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles FROM osm_relations WHERE id = ANY (?)";

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

  private static final String DELETE = "DELETE FROM osm_relations WHERE id = ?";

  private static final String COPY =
      "COPY osm_relations (id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, geom) FROM STDIN BINARY";

  private final DataSource dataSource;

  private final RelationBuilder relationBuilder;

  public PostgisRelationStore(DataSource dataSource, RelationBuilder relationBuilder) {
    this.dataSource = dataSource;
    this.relationBuilder = relationBuilder;
  }

  public Relation get(Long id) {
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
        List<Member> members = new ArrayList<>();
        Long[] refs = (Long[]) result.getArray(6).getArray();
        String[] types = (String[]) result.getArray(7).getArray();
        String[] roles = (String[]) result.getArray(8).getArray();
        for (int i = 0; i < refs.length; i++) {
          members.add(new Member(refs[i], Type.valueOf(types[i]), roles[i]));
        }
        return new Relation(new Info(id, version, timestamp, changeset, uid, tags), members);
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public List<Relation> getAll(List<Long> keys) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Relation> relations = new HashMap<>();
      while (result.next()) {
        long id = result.getLong(1);
        int version = result.getInt(2);
        int uid = result.getInt(3);
        LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
        long changeset = result.getLong(5);
        Map<String, String> tags = (Map<String, String>) result.getObject(6);
        List<Member> members = new ArrayList<>();
        Long[] refs = (Long[]) result.getArray(7).getArray();
        String[] types = (String[]) result.getArray(8).getArray();
        String[] roles = (String[]) result.getArray(9).getArray();
        for (int i = 0; i < refs.length; i++) {
          members.add(new Member(refs[i], Type.valueOf(types[i]), roles[i]));
        }
        relations.put(id, new Relation(new Info(id, version, timestamp, changeset, uid, tags), members));
      }
      return keys.stream().map(key -> relations.get(key)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(Long key, Relation value) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, key);
      statement.setInt(2, value.getInfo().getVersion());
      statement.setInt(3, value.getInfo().getUserId());
      statement.setObject(4, value.getInfo().getTimestamp());
      statement.setLong(5, value.getInfo().getChangeset());
      statement.setObject(6, value.getInfo().getTags());
      statement.setObject(7, value.getMembers().stream().mapToLong(m -> m.getRef()).toArray());
      statement.setObject(8, value.getMembers().stream().map(m -> m.getType().name()).toArray(String[]::new));
      statement.setObject(9, value.getMembers().stream().map(m -> m.getRole()).toArray(String[]::new));
      statement.setBytes(10, GeometryUtil.serialize(relationBuilder.build(value)));
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void putAll(List<Entry<Long, Relation>> entries) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      for (Entry<Long, Relation> entry : entries) {
        Long key = entry.key();
        Relation value = entry.value();
        statement.clearParameters();
        statement.setLong(1, key);
        statement.setInt(2, value.getInfo().getVersion());
        statement.setInt(3, value.getInfo().getUserId());
        statement.setObject(4, value.getInfo().getTimestamp());
        statement.setLong(5, value.getInfo().getChangeset());
        statement.setObject(6, value.getInfo().getTags());
        statement.setObject(7, value.getMembers().stream().mapToLong(m -> m.getRef()).toArray());
        statement.setObject(8, value.getMembers().stream().map(m -> m.getType().name()).toArray(String[]::new));
        statement.setObject(9, value.getMembers().stream().map(m -> m.getRole()).toArray(String[]::new));
        statement.setBytes(10, GeometryUtil.serialize(relationBuilder.build(value)));
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

  public void importAll(List<Entry<Long, Relation>> entries) {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
        writer.writeHeader();
        for (Entry<Long, Relation> entry : entries) {
          Long key = entry.key();
          Relation value = entry.value();
          writer.startRow(10);
          writer.writeLong(key);
          writer.writeInteger(value.getInfo().getVersion());
          writer.writeInteger(value.getInfo().getUserId());
          writer.writeLocalDateTime(value.getInfo().getTimestamp());
          writer.writeLong(value.getInfo().getChangeset());
          writer.writeHstore(value.getInfo().getTags());
          writer.writeLongList(value.getMembers().stream().map(m -> m.getRef()).collect(Collectors.toList()));
          writer.writeStringList(
              value.getMembers().stream().map(m -> m.getType().name()).collect(Collectors.toList()));
          writer.writeStringList(value.getMembers().stream().map(m -> m.getRole()).collect(Collectors.toList()));
          writer.writeGeometry(relationBuilder.build(value));
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new StoreException(ex);
    }
  }
}
