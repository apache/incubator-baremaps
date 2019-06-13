package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.Relation;
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

public class EntityConsumer implements Consumer<DataBlock> {

  private final PoolingDataSource pool;

  public EntityConsumer(PoolingDataSource pool) {
    checkNotNull(pool);
    this.pool = pool;
  }

  @Override
  public void accept(DataBlock block) {
    try (Connection connection = pool.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      NodeTable.copy(pgConnection, block.getNodes());
      WayTable.copy(pgConnection, block.getWays());
      //relations.saveAll(pgConnection, block.getRelations());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
