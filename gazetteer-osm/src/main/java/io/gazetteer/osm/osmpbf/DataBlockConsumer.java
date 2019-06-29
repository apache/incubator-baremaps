package io.gazetteer.osm.osmpbf;

import static com.google.common.base.Preconditions.checkNotNull;

import io.gazetteer.osm.postgis.NodeTable;
import io.gazetteer.osm.postgis.RelationTable;
import io.gazetteer.osm.postgis.WayTable;
import java.sql.Connection;
import java.util.function.Consumer;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

public class DataBlockConsumer implements Consumer<DataBlock> {

  private final PoolingDataSource pool;

  public DataBlockConsumer(PoolingDataSource pool) {
    checkNotNull(pool);
    this.pool = pool;
  }

  @Override
  public void accept(DataBlock block) {
    try (Connection connection = pool.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      NodeTable.copy(pgConnection, block.getNodes());
      WayTable.copy(pgConnection, block.getWays());
      RelationTable.copy(pgConnection, block.getRelations());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
