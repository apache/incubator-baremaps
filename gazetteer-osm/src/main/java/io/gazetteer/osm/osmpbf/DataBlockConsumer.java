package io.gazetteer.osm.osmpbf;

import de.bytefish.pgbulkinsert.PgBulkInsert;
import io.gazetteer.osm.domain.Node;
import io.gazetteer.osm.domain.Relation;
import io.gazetteer.osm.domain.Way;
import io.gazetteer.osm.postgis.NodeMapping;
import io.gazetteer.osm.postgis.RelationMapping;
import io.gazetteer.osm.postgis.WayMapping;
import io.gazetteer.osm.rocksdb.EntityStore;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public class DataBlockConsumer implements Consumer<DataBlock> {

    private final PoolingDataSource pool;

    private final PgBulkInsert<Node> nodes;
    private final PgBulkInsert<Way> ways;
    private final PgBulkInsert<Relation> relations;

    public DataBlockConsumer(EntityStore<Node> cache, PoolingDataSource pool) {
        checkNotNull(cache);
        checkNotNull(pool);
        this.pool = pool;
        this.nodes = new PgBulkInsert<>(new NodeMapping());
        this.ways = new PgBulkInsert<>(new WayMapping(cache));
        this.relations = new PgBulkInsert<>(new RelationMapping());
    }

    @Override
    public void accept(DataBlock block) {
        checkNotNull(block);
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
