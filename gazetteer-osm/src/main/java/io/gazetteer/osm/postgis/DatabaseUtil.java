package io.gazetteer.osm.postgis;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    public static final String DROP_TABLE_INFO =
            "DROP TABLE IF EXISTS osm_info";

    public static final String DROP_TABLE_USERS =
            "DROP TABLE IF EXISTS osm_users";

    public static final String DROP_TABLE_NODES =
            "DROP TABLE IF EXISTS osm_nodes";

    public static final String DROP_TABLE_WAYS =
            "DROP TABLE IF EXISTS osm_ways";

    public static final String DROP_TABLE_WAY_NODES =
            "DROP TABLE IF EXISTS osm_way_nodes";

    public static final String DROP_TABLE_RELATIONS =
            "DROP TABLE IF EXISTS osm_relations";

    public static final String DROP_TABLE_RELATION_MEMBERS =
            "DROP TABLE IF EXISTS osm_relation_members";

    public static final String CREATE_TABLE_INFO =
            "CREATE TABLE osm_info (" +
                    "version integer NOT NULL" +
                    ");";

    public static final String CREATE_TABLE_USERS =
            "CREATE TABLE osm_users (" +
                    "id int NOT NULL," +
                    "name text NOT NULL" +
                    ");";

    public static final String CREATE_TABLE_NODES =
            "CREATE TABLE osm_nodes ("
                    + "id bigint NOT NULL,"
                    + "version int NOT NULL,"
                    + "uid int NOT NULL,"
                    + "timestamp timestamp without time zone NOT NULL,"
                    + "changeset bigint NOT NULL,"
                    + "tags hstore,"
                    + "geom geometry(point)"
                    + ")";

    public static final String CREATE_TABLE_WAYS =
            "CREATE TABLE osm_ways ("
                    + "id bigint NOT NULL,"
                    + "version int NOT NULL,"
                    + "uid int NOT NULL,"
                    + "timestamp timestamp without time zone NOT NULL,"
                    + "changeset bigint NOT NULL,"
                    + "tags hstore,"
                    + "nodes bigint[],"
                    + "geom geometry"
                    + ")";

    public static final String CREATE_TABLE_WAY_NODES =
            "CREATE TABLE osm_way_nodes (" +
                    "way_id bigint NOT NULL," +
                    "node_id bigint NOT NULL," +
                    "sequence_id int NOT NULL" +
                    ");";

    public static final String CREATE_TABLE_RELATIONS =
            "CREATE TABLE osm_relations ("
                    + "id bigint NOT NULL,"
                    + "version int NOT NULL,"
                    + "uid int NOT NULL,"
                    + "timestamp timestamp without time zone NOT NULL,"
                    + "changeset bigint NOT NULL,"
                    + "tags hstore,"
                    + "members bigint[],"
                    + "geom geometry"
                    + ")";

    public static final String CREATE_TABLE_RELATION_MEMBERS =
            "CREATE TABLE osm_relation_members (" +
                    "relation_id bigint NOT NULL," +
                    "member_id bigint NOT NULL," +
                    "member_type character(1) NOT NULL," +
                    "member_role text NOT NULL," +
                    "sequence_id int NOT NULL" +
                    ");";

    public static final String CREATE_INDEX_NODES =
            "CREATE INDEX osm_nodes_idx ON osm_nodes USING gist(geom)";

    public static final String CREATE_INDEX_WAYS =
            "CREATE INDEX osm_ways_idx ON osm_ways USING gist(geom)";

    public static final String CREATE_INDEX_RELATIONS =
            "CREATE INDEX osm_relations_idx ON osm_relations USING gist(geom)";

    public static final String SELECT_NODE =
            "SELECT id, version, uid, timestamp, changeset, tags, st_asbinary(geom) FROM osm_nodes WHERE id = ?";

    public static final String INSERT_NODE =
            "INSERT INTO osm_nodes (id, version, uid, timestamp, changeset, tags, geom) VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_NODE =
            "UPDATE osm_nodes SET version = ?, uid = ?, timestamp = ?, changeset = ?, tags = ?, geom = ? WHERE id = ?";

    public static final String DELETE_NODE =
            "DELETE FROM osm_nodes WHERE id = ?";

    public static final String SELECT_WAY =
            "SELECT id, version, uid, timestamp, changeset, tags, osm_nodes, st_asbinary(geom) FROM osm_ways WHERE id = ?";

    public static final String INSERT_WAY =
            "INSERT INTO osm_ways (id, version, uid, timestamp, changeset, tags, nodes, geom) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_WAY =
            "UPDATE osm_ways SET version = ?, uid = ?, timestamp = ?, changeset = ?, tags = ?, nodes = ?, geom = ? WHERE id = ?";

    public static final String DELETE_WAY =
            "DELETE FROM osm_ways WHERE id = ?";

    public static final String SELECT_RELATION = "";

    public static final String INSERT_RELATION = "";

    public static final String UPDATE_RELATION = "";

    public static final String DELETE_RELATION = "";


    public static PoolingDataSource createPoolingDataSource(String url) {
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, null);
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);
        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(connectionPool);
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(connectionPool);
        return dataSource;
    }

    public static void dropTables(String url) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url)) {
            connection.prepareStatement(DatabaseUtil.DROP_TABLE_INFO).execute();
            connection.prepareStatement(DatabaseUtil.DROP_TABLE_USERS).execute();
            connection.prepareStatement(DatabaseUtil.DROP_TABLE_NODES).execute();
            connection.prepareStatement(DatabaseUtil.DROP_TABLE_WAYS).execute();
            connection.prepareStatement(DatabaseUtil.DROP_TABLE_WAY_NODES).execute();
            connection.prepareStatement(DatabaseUtil.DROP_TABLE_RELATIONS).execute();
            connection.prepareStatement(DatabaseUtil.DROP_TABLE_RELATION_MEMBERS).execute();
        }
    }

    public static void createTables(String url) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url)) {
            connection.prepareStatement(DatabaseUtil.CREATE_TABLE_INFO).execute();
            connection.prepareStatement(DatabaseUtil.CREATE_TABLE_USERS).execute();
            connection.prepareStatement(DatabaseUtil.CREATE_TABLE_NODES).execute();
            connection.prepareStatement(DatabaseUtil.CREATE_TABLE_WAYS).execute();
            connection.prepareStatement(DatabaseUtil.CREATE_TABLE_WAY_NODES).execute();
            connection.prepareStatement(DatabaseUtil.CREATE_TABLE_RELATIONS).execute();
            connection.prepareStatement(DatabaseUtil.CREATE_TABLE_RELATION_MEMBERS).execute();
        }
    }
}
