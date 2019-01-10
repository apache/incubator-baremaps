package gazetteer.osm.database;

import de.bytefish.pgbulkinsert.PgBulkInsert;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Consumer;

public class BulkInsertConsumer<TEntity> implements Consumer<Collection<TEntity>> {

    private final PgBulkInsert<TEntity> bulkInsert;

    private final DataSource dataSource;

    public BulkInsertConsumer(PgBulkInsert<TEntity> bulkInsert, PoolingDataSource dataSource) {
        this.bulkInsert = bulkInsert;
        this.dataSource = dataSource;
    }

    @Override
    public void accept(Collection<TEntity> entities) {
        try (Connection connection = dataSource.getConnection()) {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            bulkInsert.saveAll(pgConnection, entities);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
