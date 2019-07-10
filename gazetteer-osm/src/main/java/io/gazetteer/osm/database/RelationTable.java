package io.gazetteer.osm.database;

import io.gazetteer.osm.model.Info;
import io.gazetteer.osm.model.Member;
import io.gazetteer.osm.model.Member.Type;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.common.postgis.util.CopyWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class RelationTable {

  private static final String SELECT =
      "SELECT version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles FROM osm_relations WHERE id = ?";

  private static final String INSERT =
      "INSERT INTO osm_relations (id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles) VALUES (?, ?, ?, ?, ?, ?, ?)";

  private static final String UPDATE =
      "UPDATE osm_relations SET version = ?, uid = ?, timestamp = ?, changeset = ?, tags = ?, member_refs = ?, member_types = ?, member_roles = ? WHERE id = ?";

  private static final String DELETE =
      "DELETE FROM osm_relations WHERE id = ?";

  private static final String COPY = "COPY osm_relations (id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles) FROM STDIN BINARY";

  public static void insert(Connection connection, Relation relation) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
      statement.setLong(1, relation.getInfo().getId());
      statement.setInt(2, relation.getInfo().getVersion());
      statement.setInt(3, relation.getInfo().getUserId());
      statement.setTimestamp(4, new Timestamp(relation.getInfo().getTimestamp()));
      statement.setLong(5, relation.getInfo().getChangeset());
      statement.setObject(6, relation.getInfo().getTags());
      statement.setObject(7, relation.getMembers().stream().map(m -> m.getRef()).toArray());
      statement.setObject(8, relation.getMembers().stream().map(m -> m.getType().name()).toArray());
      statement.setObject(9, relation.getMembers().stream().map(m -> m.getRole()).toArray());
      statement.execute();
    }
  }

  public static void update(Connection connection, Relation relation) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
      statement.setInt(1, relation.getInfo().getVersion());
      statement.setInt(2, relation.getInfo().getUserId());
      statement.setTimestamp(3, new Timestamp(relation.getInfo().getTimestamp()));
      statement.setLong(4, relation.getInfo().getChangeset());
      statement.setObject(5, relation.getInfo().getTags());
      statement.setObject(6, relation.getMembers().stream().map(m -> m.getRef()).toArray());
      statement.setObject(7, relation.getMembers().stream().map(m -> m.getType().name()).toArray());
      statement.setObject(8, relation.getMembers().stream().map(m -> m.getRole()).toArray());
      statement.setLong(7, relation.getInfo().getId());
      statement.execute();
    }
  }

  public static Relation select(Connection connection, Long id) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        int version = result.getInt(1);
        int uid = result.getInt(2);
        long timestamp = result.getTimestamp(3).getTime();
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
    }
  }

  public static void delete(Connection connection, Long id) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(DELETE)) {
      statement.setLong(1, id);
      statement.execute();
    }
  }

  public static void copy(PGConnection connection, List<Relation> relations) throws Exception {
    try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(connection, COPY))) {
      writer.writeHeader();
      for (Relation relation : relations) {
        writer.startRow(9);
        writer.writeLong(relation.getInfo().getId());
        writer.writeInteger(relation.getInfo().getVersion());
        writer.writeInteger(relation.getInfo().getUserId());
        writer.writeLong(relation.getInfo().getTimestamp());
        writer.writeLong(relation.getInfo().getChangeset());
        writer.writeHstore(relation.getInfo().getTags());
        writer.writeLongList(relation.getMembers().stream().map(m -> m.getRef()).collect(Collectors.toList()));
        writer.writeStringList(relation.getMembers().stream().map(m -> m.getType().name()).collect(Collectors.toList()));
        writer.writeStringList(relation.getMembers().stream().map(m -> m.getRole()).collect(Collectors.toList()));
      }
    }
  }

}
