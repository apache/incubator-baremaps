package io.gazetteer.osm.pgbulkinsert;

import io.gazetteer.osm.postgis.PostgisSchema;
import org.apache.commons.dbcp2.PoolingDataSource;

public class PgBulkInsertUtil {

  public static PgBulkInsertConsumer consumer(String url) {
    PoolingDataSource pool = PostgisSchema.createPoolingDataSource(url);
    return new PgBulkInsertConsumer(pool);
  }
}
