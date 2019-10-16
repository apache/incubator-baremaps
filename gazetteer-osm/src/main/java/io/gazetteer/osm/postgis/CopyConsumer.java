package io.gazetteer.osm.postgis;

import static com.google.common.base.Preconditions.checkNotNull;

import io.gazetteer.common.postgis.CopyWriter;
import io.gazetteer.osm.geometry.NodeGeometryBuilder;
import io.gazetteer.osm.geometry.RelationGeometryBuilder;
import io.gazetteer.osm.geometry.WayGeometryBuilder;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.FileBlockConsumer;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import io.gazetteer.osm.osmpbf.PrimitiveBlock;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;

public class CopyConsumer extends FileBlockConsumer {

  private final DataSource pool;

  private final NodeGeometryBuilder nodeGeometryBuilder;
  private final WayGeometryBuilder wayGeometryBuilder;
  private final RelationGeometryBuilder relationGeometryBuilder;

  private static final String COPY_NODE =
      "COPY osm_nodes (id, version, uid, timestamp, changeset, tags, geom) FROM STDIN BINARY";
  private static final String COPY_WAY =
      "COPY osm_ways (id, version, uid, timestamp, changeset, tags, nodes, geom) FROM STDIN BINARY";
  private static final String COPY_RELATION =
      "COPY osm_relations (id, version, uid, timestamp, changeset, tags, member_refs, member_types, member_roles, geom) FROM STDIN BINARY";

  public CopyConsumer(DataSource pool, NodeGeometryBuilder nodeGeometryBuilder, WayGeometryBuilder wayGeometryBuilder, RelationGeometryBuilder relationGeometryBuilder) {
    checkNotNull(pool);
    this.pool = pool;
    this.nodeGeometryBuilder = nodeGeometryBuilder;
    this.wayGeometryBuilder = wayGeometryBuilder;
    this.relationGeometryBuilder = relationGeometryBuilder;
  }

  @Override
  public void accept(HeaderBlock headerBlock) {
    try (Connection connection = pool.getConnection()) {
      HeaderTable.insert(connection, headerBlock);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void accept(PrimitiveBlock primitiveBlock) {
    try (Connection connection = pool.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      copyNodes(pgConnection, primitiveBlock.getDenseNodes());
      copyWays(pgConnection, primitiveBlock.getWays());
      copyRelations(pgConnection, primitiveBlock.getRelations());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void copyNodes(PGConnection connection, List<Node> nodes) throws Exception {
    try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(connection, COPY_NODE))) {
      writer.writeHeader();
      for (Node node : nodes) {
        writer.startRow(7);
        writer.writeLong(node.getInfo().getId());
        writer.writeInteger(node.getInfo().getVersion());
        writer.writeInteger(node.getInfo().getUserId());
        writer.writeLocalDateTime(node.getInfo().getTimestamp());
        writer.writeLong(node.getInfo().getChangeset());
        writer.writeHstore(node.getInfo().getTags());
        writer.writeGeometry(nodeGeometryBuilder.create(node));
      }
    }
  }

  private void copyWays(PGConnection connection, List<Way> ways) throws Exception {
    try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(connection, COPY_WAY))) {
      writer.writeHeader();
      for (Way way : ways) {
        writer.startRow(8);
        writer.writeLong(way.getInfo().getId());
        writer.writeInteger(way.getInfo().getVersion());
        writer.writeInteger(way.getInfo().getUserId());
        writer.writeLocalDateTime(way.getInfo().getTimestamp());
        writer.writeLong(way.getInfo().getChangeset());
        writer.writeHstore(way.getInfo().getTags());
        writer.writeLongList(way.getNodes());
        writer.writeGeometry(wayGeometryBuilder.create(way));
      }
    }
  }

  private void copyRelations(PGConnection connection, List<Relation> relations) throws Exception {
    try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(connection, COPY_RELATION))) {
      writer.writeHeader();
      for (Relation relation : relations) {
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
  }

}
