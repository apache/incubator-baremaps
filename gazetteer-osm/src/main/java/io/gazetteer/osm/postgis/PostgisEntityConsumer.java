package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.postgis.util.CopyWriter;
import java.util.List;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.util.function.Consumer;
import org.postgresql.copy.PGCopyOutputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.gazetteer.osm.util.GeometryUtil.asGeometry;

public class PostgisEntityConsumer implements Consumer<DataBlock> {

  private final PoolingDataSource pool;

  private static final String COPY_NODES = "COPY osm_nodes (id, version, uid, timestamp, changeset, tags, geom) FROM STDIN BINARY";

  private static final String COPY_WAYS = "COPY osm_ways (id, version, uid, timestamp, changeset, tags, nodes) FROM STDIN BINARY";

  private static final String COPY_RELATIONS = "COPY osm_relations (id, version, uid, timestamp, changeset, tags, nodes) FROM STDIN BINARY";

  public PostgisEntityConsumer(PoolingDataSource pool) {
    checkNotNull(pool);
    this.pool = pool;
  }

  @Override
  public void accept(DataBlock block) {
    try (Connection connection = pool.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      copyNodes(pgConnection, block.getNodes());
      copyWays(pgConnection, block.getWays());
      //relations.saveAll(pgConnection, block.getRelations());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void copyNodes(PGConnection connection, List<Node> nodes) throws Exception {
    try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(connection, COPY_NODES))) {
      writer.writeHeader();
      for (Node node : nodes) {
        writer.startRow(7);
        writer.writeLong(node.getInfo().getId());
        writer.writeInteger(node.getInfo().getVersion());
        writer.writeInteger(node.getInfo().getUserId());
        writer.writeLong(node.getInfo().getTimestamp());
        writer.writeLong(node.getInfo().getChangeset());
        writer.writeHstore(node.getInfo().getTags());
        writer.writeGeometry(asGeometry(node));
      }
    }
  }

  public static void copyWays(PGConnection connection, List<Way> ways) throws Exception {
    try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(connection, COPY_WAYS))) {
      writer.writeHeader();
      for (Way way : ways) {
        writer.startRow(7);
        writer.writeLong(way.getInfo().getId());
        writer.writeInteger(way.getInfo().getVersion());
        writer.writeInteger(way.getInfo().getUserId());
        writer.writeLong(way.getInfo().getTimestamp());
        writer.writeLong(way.getInfo().getChangeset());
        writer.writeHstore(way.getInfo().getTags());
        writer.writeLongList(way.getNodes());
      }
    }
  }

  public static void copyRelations(PGConnection connection, List<Relation> relations) throws Exception {
    try (CopyWriter writer = new CopyWriter(new PGCopyOutputStream(connection, COPY_RELATIONS))) {
      writer.writeHeader();
      for (Relation relation : relations) {
        writer.startRow(7);
        writer.writeLong(relation.getInfo().getId());
        writer.writeInteger(relation.getInfo().getVersion());
        writer.writeInteger(relation.getInfo().getUserId());
        writer.writeLong(relation.getInfo().getTimestamp());
        writer.writeLong(relation.getInfo().getChangeset());
        writer.writeHstore(relation.getInfo().getTags());
        //writer.writeLongList(relation.getMembers().stream().map());
      }
    }
  }

}
