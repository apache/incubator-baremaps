package io.gazetteer.osm.postgis;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DatabaseUtil {

    public static final String DROP_TABLE_NODES = "DROP TABLE IF EXISTS osm_nodes";

    public static final String DROP_TABLE_WAYS = "DROP TABLE IF EXISTS osm_ways";

    public static final String DROP_TABLE_RELATIONS = "DROP TABLE IF EXISTS osm_relations";

    public static final String CREATE_TABLE_NODES =
            "CREATE TABLE osm_nodes (" +
            "id bigint NOT NULL," +
            "version int NOT NULL," +
            "uid int NOT NULL," +
            "timestamp timestamp without time zone NOT NULL," +
            "changeset bigint NOT NULL," +
            "tags hstore," +
            "geom geometry(point)" +
            ")";

    public static final String CREATE_TABLE_WAYS =
            "CREATE TABLE osm_ways (" +
            "id bigint NOT NULL," +
            "version int NOT NULL," +
            "uid int NOT NULL," +
            "timestamp timestamp without time zone NOT NULL," +
            "changeset bigint NOT NULL," +
            "tags hstore," +
            "osm_nodes bigint[]," +
            "geom geometry" +
            ")";

    public static final String CREATE_TABLE_RELATIONS = 
            "CREATE TABLE osm_relations (" +
            "id bigint NOT NULL," +
            "version int NOT NULL," +
            "uid int NOT NULL," +
            "timestamp timestamp without time zone NOT NULL," +
            "changeset bigint NOT NULL," +
            "tags hstore," +
            "members bigint[]," +
            "geom geometry" +
            ")";

    public static final String CREATE_INDEX_WAYS =
            "CREATE INDEX osm_ways_idx ON osm_ways USING gist(geom)";


    public static PoolingDataSource create(String conn) {
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(conn, null);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(connectionPool);
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(connectionPool);
        return dataSource;
    }

}
