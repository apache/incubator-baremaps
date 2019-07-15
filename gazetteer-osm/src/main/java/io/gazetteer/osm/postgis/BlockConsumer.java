package io.gazetteer.osm.postgis;

import static com.google.common.base.Preconditions.checkNotNull;

import io.gazetteer.osm.osmpbf.PrimitiveBlock;
import io.gazetteer.osm.osmpbf.FileBlockConsumer;
import io.gazetteer.osm.osmpbf.HeaderBlock;
import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

public class BlockConsumer extends FileBlockConsumer {

  private final DataSource pool;

  public BlockConsumer(DataSource pool) {
    checkNotNull(pool);
    this.pool = pool;
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
      NodeTable.copy(pgConnection, primitiveBlock.getNodes());
      WayTable.copy(pgConnection, primitiveBlock.getWays());
      RelationTable.copy(pgConnection, primitiveBlock.getRelations());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
