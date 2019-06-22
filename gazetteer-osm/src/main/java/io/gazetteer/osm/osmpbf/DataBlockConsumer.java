package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.model.Relation;
import io.gazetteer.osm.osmpbf.DataBlock;
import io.gazetteer.osm.postgis.NodeTable;
import io.gazetteer.osm.postgis.RelationTable;
import io.gazetteer.osm.postgis.WayTable;
import io.gazetteer.postgis.util.CopyWriter;
import java.util.List;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.util.function.Consumer;
import org.postgresql.copy.PGCopyOutputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.gazetteer.osm.util.GeometryUtil.asGeometry;

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
