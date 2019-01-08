package gazetteer.osm.postgres;

import de.bytefish.pgbulkinsert.PgBulkInsert;
import gazetteer.osm.leveldb.EntityStore;
import gazetteer.osm.model.Node;
import gazetteer.osm.model.PrimitiveBlock;
import gazetteer.osm.model.Way;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class PrimitiveBlockConsumer implements Consumer<PrimitiveBlock> {

    private final EntityStore<Node> nodeStore;
    private final PoolingDataSource dataSource;

    private final PgBulkInsert<Node> nodeDatabase;
    private final PgBulkInsert<Way> wayDatabase;

    public PrimitiveBlockConsumer(EntityStore<Node> nodeStore, PoolingDataSource dataSource) {
        this.nodeStore = nodeStore;
        this.dataSource = dataSource;
        this.nodeDatabase = new PgBulkInsert<>(new NodeMapping());
        this.wayDatabase = new PgBulkInsert<>(new WayMapping(nodeStore));
    }

    @Override
    public void accept(PrimitiveBlock primitiveBlock) {
        try (Connection connection = dataSource.getConnection()) {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            Collection<Node> nodes = primitiveBlock.getNodes();
            this.nodeDatabase.saveAll(pgConnection, nodes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
