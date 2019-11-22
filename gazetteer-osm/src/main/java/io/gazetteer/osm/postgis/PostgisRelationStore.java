package io.gazetteer.osm.postgis;

import static io.gazetteer.common.postgis.GeometryUtils.toWKB;

import io.gazetteer.common.postgis.CopyWriter;
import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Member;
import io.gazetteer.osm.model.Member.Type;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Store;
import io.gazetteer.osm.model.StoreException;
import io.gazetteer.osm.geometry.RelationGeometryBuilder;
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
import org.locationtech.jts.geom.Geometry;
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

  private static final String DELETE =
      "DELETE FROM osm_relations WHERE id = ?";

  private static final String COPY =
      "COPY osm_relations (id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, geom) FROM STDIN BINARY";

  private final DataSource dataSource;

  private final RelationGeometryBuilder relationGeometryBuilder;

  public PostgisRelationStore(DataSource dataSource, RelationGeometryBuilder relationGeometryBuilder) {
    this.dataSource = dataSource;
    this.relationGeometryBuilder = relationGeometryBuilder;
  }

  public Relation get(Long id) {
    try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        int version = result.getInt(1);
        int uid = result.getInt(2);
        LocalDateTime timestamp = result.getObject(3, LocalDateTime.class);
        long changeset = result.getLong(4);
        Map<String, String> tags = (Map<String, String>) result.getObject(5);
        List<Member> members = new ArrayList<>();
        Long[] refs = (Long[]) result.getObject(6);
        String[] types = (String[]) result.getObject(7);
        String[] roles = (String[]) result.getObject(8);
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
    try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
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
        Long[] refs = (Long[]) result.getObject(7);
        String[] types = (String[]) result.getObject(8);
        String[] roles = (String[]) result.getObject(9);
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
    try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, value.getInfo().getId());
      statement.setInt(2, value.getInfo().getVersion());
      statement.setInt(3, value.getInfo().getUserId());
      statement.setObject(4, value.getInfo().getTimestamp());
      statement.setLong(5, value.getInfo().getChangeset());
      statement.setObject(6, value.getInfo().getTags());
      statement.setObject(7, value.getMembers().stream().mapToLong(m -> m.getRef()).toArray());
      statement.setObject(8, value.getMembers().stream().map(m -> m.getType().name()).toArray(String[]::new));
      statement.setObject(9, value.getMembers().stream().map(m -> m.getRole()).toArray(String[]::new));
      statement.setBytes(10, toWKB(relationGeometryBuilder.create(value)));
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void putAll(List<Entry<Long, Relation>> entries) {
    throw new UnsupportedOperationException();
  }

  public void delete(Long id) {
    try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, id);
      statement.execute();
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void deleteAll(List<Long> keys) {
    throw new UnsupportedOperationException();
  }

  public void importAll(List<Entry<Long, Relation>> entries) {
    try (Connection connection = dataSource.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(pgConnection, COPY))) {
        writer.writeHeader();
        for (Entry<Long, Relation> entry : entries) {
          Relation relation = entry.value();
          writer.startRow(10);
          writer.writeLong(relation.getInfo().getId());
          writer.writeInteger(relation.getInfo().getVersion());
          writer.writeInteger(relation.getInfo().getUserId());
          writer.writeLocalDateTime(relation.getInfo().getTimestamp());
          writer.writeLong(relation.getInfo().getChangeset());
          writer.writeHstore(relation.getInfo().getTags());
          writer.writeLongList(relation.getMembers().stream().map(m -> m.getRef()).collect(Collectors.toList()));
          writer.writeStringList(relation.getMembers().stream().map(m -> m.getType().name()).collect(Collectors.toList()));
          writer.writeStringList(relation.getMembers().stream().map(m -> m.getRole()).collect(Collectors.toList()));
          Geometry geom = relationGeometryBuilder.create(relation);
          writer.writeGeometry(geom);
        }
      }
    } catch (Exception e) {
      throw new StoreException(e);
    }
  }

}
