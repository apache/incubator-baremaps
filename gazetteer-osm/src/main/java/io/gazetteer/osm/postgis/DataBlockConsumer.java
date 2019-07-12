package io.gazetteer.osm.postgis;

import static com.google.common.base.Preconditions.checkNotNull;

import io.gazetteer.osm.osmpbf.Data;
import io.gazetteer.osm.osmpbf.FileBlockConsumer;
import io.gazetteer.osm.osmpbf.Header;
import io.gazetteer.osm.postgis.HeaderTable;
import io.gazetteer.osm.postgis.NodeTable;
import io.gazetteer.osm.postgis.RelationTable;
import io.gazetteer.osm.postgis.WayTable;
import java.sql.Connection;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

public class DataBlockConsumer extends FileBlockConsumer {

  private final PoolingDataSource pool;

  public DataBlockConsumer(PoolingDataSource pool) {
    checkNotNull(pool);
    this.pool = pool;
  }

  @Override
  public void accept(Header header) {
    try (Connection connection = pool.getConnection()) {
      HeaderTable.insert(connection, header);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void accept(Data data) {
    try (Connection connection = pool.getConnection()) {
      PGConnection pgConnection = connection.unwrap(PGConnection.class);
      NodeTable.copy(pgConnection, data.getNodes());
      WayTable.copy(pgConnection, data.getWays());
      RelationTable.copy(pgConnection, data.getRelations());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
