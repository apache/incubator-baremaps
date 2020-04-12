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

import com.baremaps.core.postgis.CopyWriter;
import com.baremaps.osm.database.RelationTable.Relation;
import com.baremaps.osm.geometry.GeometryUtil;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
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

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Relation relation = (Relation) o;
      return id == relation.id &&
          version == relation.version &&
          changeset == relation.changeset &&
          userId == relation.userId &&
          Objects.equal(timestamp, relation.timestamp) &&
          Objects.equal(tags, relation.tags) &&
          Arrays.deepEquals(memberRefs, relation.memberRefs) &&
          Arrays.deepEquals(memberTypes, relation.memberTypes) &&
          Arrays.deepEquals(memberRoles, relation.memberRoles) &&
          Objects.equal(geometry, relation.geometry);
    }

    @Override
    public int hashCode() {
      return Objects
          .hashCode(id, version, timestamp, changeset, userId, tags, memberRefs, memberTypes, memberRoles,
              geometry);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("version", version)
          .add("timestamp", timestamp)
          .add("changeset", changeset)
          .add("userId", userId)
          .add("tags", tags)
          .add("memberRefs", memberRefs)
          .add("memberTypes", memberTypes)
          .add("memberRoles", memberRoles)
          .add("geometry", geometry)
          .toString();
    }
  }

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, st_asbinary(ST_Transform(geom, 4326)) FROM osm_relations WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, st_asbinary(ST_Transform(geom, 4326)) FROM osm_relations WHERE id = ANY (?)";

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

  public RelationTable(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Relation select(Long id) {
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
        return new RelationTable.Relation(id, version, timestamp, changeset, uid, tags, refs, types, roles,
            geometry);
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<Relation> select(List<Long> ids) {
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
        relations.put(id,
            new Relation(id, version, timestamp, changeset, uid, tags, refs, types, roles, geometry));
      }
      return ids.stream().map(id -> relations.get(id)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  public void insert(Relation row) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
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
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void insert(List<Relation> rows) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT)) {
      for (Relation row : rows) {
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
      throw new DatabaseException(e);
    }
  }

  public void delete(Long id) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, id);
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e);
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
      throw new DatabaseException(e);
    }
  }

  public void copy(List<Relation> rows) {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
        writer.writeHeader();
        for (Relation row : rows) {
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
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new DatabaseException(ex);
    }
  }

}
