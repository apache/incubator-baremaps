package io.gazetteer.osm.pgbulkinsert;

import de.bytefish.pgbulkinsert.PgBulkInsert;
import io.gazetteer.osm.model.DataStore;
import io.gazetteer.osm.model.Node;
import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.model.Way;
import io.gazetteer.osm.osmpbf.DataBlock;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class PgBulkInsertConsumer implements Consumer<DataBlock> {

  private final PoolingDataSource pool;

  private final PgBulkInsert<Node> nodes;
  private final PgBulkInsert<Way> ways;
  private final PgBulkInsert<Relation> relations;

  public PgBulkInsertConsumer(DataStore<Long, Node> cache, PoolingDataSource pool) {
    checkNotNull(cache);
    checkNotNull(pool);
    this.pool = pool;
    this.nodes = new PgBulkInsert<>(new NodeMapping());
    this.ways = new PgBulkInsert<>(new WayMapping(cache));
    this.relations = new PgBulkInsert<>(new RelationMapping());
  }

  @Override
  public void accept(DataBlock block) {
    try (Connection connection = pool.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      nodes.saveAll(pgConnection, block.getNodes());
      ways.saveAll(pgConnection, block.getWays());
      relations.saveAll(pgConnection, block.getRelations());
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
