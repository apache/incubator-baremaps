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
import com.baremaps.osm.model.Member;
import com.baremaps.osm.model.Member.MemberType;
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

public class RelationTable implements Table<Relation> {

  private final String select;

  private final String selectIn;

  private final String insert;

  private final String delete;

  private final String copy;

  private final DataSource dataSource;

  public RelationTable(DataSource dataSource) {
    this(dataSource, "osm_relations", "id", "version", "uid", "timestamp", "changeset", "tags", "member_refs",
        "member_types", "member_roles", "geom");
  }

  @Inject
  public RelationTable(DataSource dataSource, String nodeTable, String idColumn, String versionColumn, String uidColumn,
      String timestampColumn, String changesetColumn, String tagsColumn,
      String memberRefs, String memberTypes, String memberRoles, String geometryColumn) {
    this.dataSource = dataSource;
    this.select = String.format(
        "SELECT %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s, st_asbinary(%11$s) FROM %1$s WHERE %2$s = ?",
        nodeTable, idColumn, versionColumn, uidColumn, timestampColumn,
        changesetColumn, tagsColumn, memberRefs, memberTypes, memberRoles, geometryColumn);
    this.selectIn = String.format(
        "SELECT %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s, st_asbinary(%11$s) FROM %1$s WHERE %2$s = ANY (?)",
        nodeTable, idColumn, versionColumn, uidColumn, timestampColumn,
        changesetColumn, tagsColumn, memberRefs, memberTypes, memberRoles, geometryColumn);
    this.insert = String.format(
        "INSERT INTO %1$s (%2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s, %11$s) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT (%2$s) DO UPDATE SET "
            + "%3$s = excluded.%3$s, "
            + "%4$s = excluded.%4$s, "
            + "%5$s = excluded.%5$s, "
            + "%6$s = excluded.%6$s, "
            + "%7$s = excluded.%7$s, "
            + "%8$s = excluded.%8$s, "
            + "%9$s = excluded.%9$s, "
            + "%10$s = excluded.%10$s, "
            + "%11$s = excluded.%11$s",
        nodeTable, idColumn, versionColumn, uidColumn, timestampColumn,
        changesetColumn, tagsColumn, memberRefs, memberTypes, memberRoles, geometryColumn);
    this.delete = String.format(
        "DELETE FROM %1$s WHERE %2$s = ?",
        nodeTable, idColumn);
    this.copy = String.format(
        "COPY %1$s (%2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s, %11$s) FROM STDIN BINARY",
        nodeTable, idColumn, versionColumn, uidColumn, timestampColumn,
        changesetColumn, tagsColumn, memberRefs, memberTypes, memberRoles, geometryColumn);
  }

  public Relation select(Long id) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(select)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        return getRelation(result);
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public List<Relation> select(List<Long> ids) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(selectIn)) {
      statement.setArray(1, connection.createArrayOf("int8", ids.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Relation> relations = new HashMap<>();
      while (result.next()) {
        Relation relation = getRelation(result);
        relations.put(relation.getId(), relation);
      }
      return ids.stream().map(id -> relations.get(id)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private Relation getRelation(ResultSet result) throws SQLException {
    long id = result.getLong(1);
    int version = result.getInt(2);
    int uid = result.getInt(3);
    LocalDateTime timestamp = result.getObject(4, LocalDateTime.class);
    long changeset = result.getLong(5);
    Map<String, String> tags = (Map<String, String>) result.getObject(6);
    Long[] refs = (Long[]) result.getArray(7).getArray();
    Integer[] types = (Integer[]) result.getArray(8).getArray();
    String[] roles = (String[]) result.getArray(9).getArray();
    List<Member> members = new ArrayList<>();
    for (int i = 0; i < refs.length; i++) {
      members.add(new Member(refs[i], MemberType.forNumber(types[i]), roles[i]));
    }
    Geometry geometry = GeometryUtil.deserialize(result.getBytes(10));
    return new Relation(id, version, timestamp, changeset, uid, tags, members, geometry);
  }

  public void insert(Relation entity) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      setRelation(statement, entity);
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  @Override
  public void insert(List<Relation> entities) throws DatabaseException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      for (Relation entity : entities) {
        statement.clearParameters();
        setRelation(statement, entity);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private void setRelation(PreparedStatement statement, Relation entity) throws SQLException {
    statement.setLong(1, entity.getId());
    statement.setInt(2, entity.getVersion());
    statement.setInt(3, entity.getUserId());
    statement.setObject(4, entity.getTimestamp());
    statement.setLong(5, entity.getChangeset());
    statement.setObject(6, entity.getTags());
    Object[] refs = entity.getMembers().stream().map(Member::getRef).toArray();
    statement.setObject(7, statement.getConnection().createArrayOf("bigint", refs));
    Object[] types = entity.getMembers().stream().map(m -> m.getType().getNumber()).toArray();
    statement.setObject(8, statement.getConnection().createArrayOf("int", types));
    Object[] roles = entity.getMembers().stream().map(Member::getRole).toArray();
    statement.setObject(9, statement.getConnection().createArrayOf("varchar", roles));
    statement.setBytes(10, GeometryUtil.serialize(entity.getGeometry().orElse(null)));
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

  @Override
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

  public void copy(List<Relation> entities) throws DatabaseException {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, copy))) {
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
          writer.writeIntegerList(entity.getMembers().stream().map(Member::getType).map(MemberType::getNumber)
              .collect(Collectors.toList()));
          writer.writeStringList(entity.getMembers().stream().map(Member::getRole).collect(Collectors.toList()));
          writer.writeGeometry(entity.getGeometry().orElse(null));
        }
      }
    } catch (IOException | SQLException ex) {
      throw new DatabaseException(ex);
    }
  }

}
