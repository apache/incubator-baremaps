package gazetteer.osm.osmpbf;

import de.bytefish.pgbulkinsert.PgBulkInsert;
import gazetteer.osm.cache.EntityCache;
import gazetteer.osm.database.NodeMapping;
import gazetteer.osm.database.WayMapping;
import gazetteer.osm.model.Node;
import gazetteer.osm.model.Way;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class PrimitiveBlockConsumer implements Consumer<PrimitiveBlock> {

    private final EntityCache<Node> nodeStore;
    private final PoolingDataSource dataSource;

    private final PgBulkInsert<Node> dbNodes;
    private final PgBulkInsert<Way> dbWays;

    public PrimitiveBlockConsumer(EntityCache<Node> nodeStore, PoolingDataSource dataSource) {
        this.nodeStore = nodeStore;
        this.dataSource = dataSource;
        this.dbNodes = new PgBulkInsert<>(new NodeMapping());
        this.dbWays = new PgBulkInsert<>(new WayMapping(nodeStore));
    }

    @Override
    public void accept(PrimitiveBlock block) {
        try (Connection connection = dataSource.getConnection()) {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            List<Way> ways = block.getWays();
            dbWays.saveAll(pgConnection, ways);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
