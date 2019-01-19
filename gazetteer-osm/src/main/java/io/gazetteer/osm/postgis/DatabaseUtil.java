package io.gazetteer.osm.postgis;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DatabaseUtil {

    public static final String DROP_TABLE_NODES = "DROP TABLE IF EXISTS nodes";

    public static final String DROP_TABLE_WAYS = "DROP TABLE IF EXISTS ways";

    public static final String DROP_TABLE_RELATIONS = "DROP TABLE IF EXISTS relations";

    public static final String CREATE_TABLE_NODES =
            "CREATE TABLE nodes (" +
            "    id bigint NOT NULL," +
            "    version int NOT NULL," +
            "    uid int NOT NULL," +
            "    timestamp timestamp without time zone NOT NULL," +
            "    changeset bigint NOT NULL," +
            "    tags hstore," +
            "    geom geometry(point)" +
            ")";

    public static final String CREATE_TABLE_WAYS =
            "CREATE TABLE ways (" +
            "    id bigint NOT NULL," +
            "    version int NOT NULL," +
            "    uid int NOT NULL," +
            "    timestamp timestamp without time zone NOT NULL," +
            "    changeset bigint NOT NULL," +
            "    tags hstore," +
            "    nodes bigint[]," +
            "    geom geometry" +
            ")";

    public static final String CREATE_TABLE_RELATIONS = 
            "CREATE TABLE relations (" +
            "    id bigint NOT NULL," +
            "    version int NOT NULL," +
            "    uid int NOT NULL," +
            "    timestamp timestamp without time zone NOT NULL," +
            "    changeset bigint NOT NULL," +
            "    tags hstore," +
            "    members bigint[]," +
            "    geom geometry" +
            ")";

    public static final String CREATE_INDEX_WAYS =
            "CREATE INDEX ways_idx ON ways USING gist(geom)";


    public static PoolingDataSource create(String conn) {
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(conn, null);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(connectionPool);
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(connectionPool);
        return dataSource;
    }

}
