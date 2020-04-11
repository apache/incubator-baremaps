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

package com.baremaps.osm.database;

import com.baremaps.osm.database.RelationTable.Relation;
import com.baremaps.osm.geometry.GeometryUtil;
import com.baremaps.osm.geometry.RelationBuilder;
import com.baremaps.osm.store.StoreException;
import com.baremaps.core.postgis.CopyWriter;
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

public class RelationTable implements Table<Relation> {

  public static class Relation {

    private final long id;

    private final int version;

    private final LocalDateTime timestamp;

    private final long changeset;

    private final int userId;

    private final Map<String, String> tags;

    private final Long[] memberRefs;

    private final String[] memberTypes;

    private final String[] memberRoles;

    private final Geometry geometry;

    public Relation(
        long id,
        int version,
        LocalDateTime timestamp,
        long changeset,
        int userId,
        Map<String, String> tags,
        Long[] memberRefs,
        String[] memberTypes,
        String[] memberRoles,
        Geometry geometry) {
      this.id = id;
      this.version = version;
      this.timestamp = timestamp;
      this.changeset = changeset;
      this.userId = userId;
      this.tags = tags;
      this.memberRefs = memberRefs;
      this.memberTypes = memberTypes;
      this.memberRoles = memberRoles;
      this.geometry = geometry;
    }

    public long getId() {
      return id;
    }

    public int getVersion() {
      return version;
    }

    public LocalDateTime getTimestamp() {
      return timestamp;
    }

    public long getChangeset() {
      return changeset;
    }

    public int getUserId() {
      return userId;
    }

    public Map<String, String> getTags() {
      return tags;
    }

    public Long[] getMemberRefs() {
      return memberRefs;
    }

    public String[] getMemberTypes() {
      return memberTypes;
    }

    public String[] getMemberRoles() {
      return memberRoles;
    }

    public Geometry getGeometry() {
      return geometry;
    }

  }

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, geom FROM osm_relations WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, geom FROM osm_relations WHERE id = ANY (?)";

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

  private final RelationBuilder relationBuilder;

  public RelationTable(DataSource dataSource, RelationBuilder relationBuilder) {
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
        Long[] refs = (Long[]) result.getArray(6).getArray();
        String[] types = (String[]) result.getArray(7).getArray();
        String[] roles = (String[]) result.getArray(8).getArray();
        Geometry geometry = GeometryUtil.deserialize(result.getBytes(9));
        return new RelationTable.Relation(id, version, timestamp, changeset, uid, tags, refs, types, roles, geometry);
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public List<Relation> getAll(List<Long> ids) {
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
        Geometry geometry = GeometryUtil.deserialize(result.getBytes(10));
        relations.put(id, new Relation(id, version, timestamp, changeset, uid, tags, refs, types, roles, geometry));
      }
      return ids.stream().map(id -> relations.get(id)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  public void put(Relation value) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, value.getId());
      statement.setInt(2, value.getVersion());
      statement.setInt(3, value.getUserId());
      statement.setObject(4, value.getTimestamp());
      statement.setLong(5, value.getChangeset());
      statement.setObject(6, value.getTags());
      statement.setObject(7, value.getMemberRefs());
      statement.setObject(8, value.getMemberTypes());
      statement.setObject(9, value.getMemberRoles());
      statement.setBytes(10, GeometryUtil.serialize(value.getGeometry()));
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void putAll(List<Relation> entries) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      for (Relation value : entries) {
        statement.clearParameters();
        statement.setLong(1, value.getId());
        statement.setInt(2, value.getVersion());
        statement.setInt(3, value.getUserId());
        statement.setObject(4, value.getTimestamp());
        statement.setLong(5, value.getChangeset());
        statement.setObject(6, value.getTags());
        statement.setObject(7, value.getMemberRefs());
        statement.setObject(8, value.getMemberTypes());
        statement.setObject(9, value.getMemberRoles());
        statement.setBytes(10, GeometryUtil.serialize(value.getGeometry()));
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

  public void importAll(List<Relation> entries) {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
        writer.writeHeader();
        for (Relation value : entries) {
          writer.startRow(10);
          writer.writeLong(value.getId());
          writer.writeInteger(value.getVersion());
          writer.writeInteger(value.getUserId());
          writer.writeLocalDateTime(value.getTimestamp());
          writer.writeLong(value.getChangeset());
          writer.writeHstore(value.getTags());
          writer.writeLongList(Arrays.asList(value.getMemberRefs()));
          writer.writeStringList(Arrays.asList(value.getMemberTypes()));
          writer.writeStringList(Arrays.asList(value.getMemberRoles()));
          writer.writeGeometry(value.getGeometry());
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new StoreException(ex);
    }
  }

  public RelationBuilder getRelationBuilder() {
    return relationBuilder;
  }

}
