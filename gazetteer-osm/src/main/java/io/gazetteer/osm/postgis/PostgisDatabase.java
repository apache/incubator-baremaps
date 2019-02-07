package io.gazetteer.osm.postgis;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.sql.Connection;
import java.sql.SQLException;

import static io.gazetteer.osm.postgis.NodeTable.CREATE_TABLE_NODES;
import static io.gazetteer.osm.postgis.NodeTable.DROP_TABLE_NODES;

public class PostgisDatabase {

  public static final String CREATE_EXTENSION_HSTORE = "CREATE EXTENSION IF NOT EXISTS hstore";

  public static final String CREATE_EXTENSION_POSTGIS = "CREATE EXTENSION IF NOT EXISTS postgis";

  public static final String DROP_TABLE_INFO = "DROP TABLE IF EXISTS osm_info";

  public static final String DROP_TABLE_USERS = "DROP TABLE IF EXISTS osm_users";

  public static final String DROP_TABLE_WAYS = "DROP TABLE IF EXISTS osm_ways";

  public static final String DROP_TABLE_RELATIONS = "DROP TABLE IF EXISTS osm_relations";

  public static final String CREATE_TABLE_INFO =
      "CREATE TABLE IF NOT EXISTS osm_info (" + "version integer NOT NULL" + ");";

  public static final String CREATE_TABLE_USERS =
      "CREATE TABLE IF NOT EXISTS osm_users (" + "id int NOT NULL," + "name text NOT NULL" + ");";

  public static final String CREATE_TABLE_WAYS =
      "CREATE TABLE IF NOT EXISTS osm_ways ("
          + "id bigint NOT NULL,"
          + "version int NOT NULL,"
          + "uid int NOT NULL,"
          + "timestamp timestamp without time zone NOT NULL,"
          + "changeset bigint NOT NULL,"
          + "tags hstore,"
          + "nodes bigint[],"
          + "geom geometry"
          + ")";

  public static final String CREATE_TABLE_RELATIONS =
      "CREATE TABLE IF NOT EXISTS osm_relations ("
          + "id bigint NOT NULL,"
          + "version int NOT NULL,"
          + "uid int NOT NULL,"
          + "timestamp timestamp without time zone NOT NULL,"
          + "changeset bigint NOT NULL,"
          + "tags hstore,"
          + "member_refs bigint[],"
          + "member_types character(1)[],"
          + "member_roles text[],"
          + "geom geometry"
          + ")";

  public static final String CREATE_INDEX_WAYS =
      "CREATE INDEX IF NOT EXISTS osm_ways_idx ON osm_ways USING gist(geom)";

  public static final String CREATE_INDEX_RELATIONS =
      "CREATE INDEX IF NOT EXISTS osm_relations_idx ON osm_relations USING gist(geom)";


  public static final String SELECT_WAY =
      "SELECT id, version, uid, timestamp, changeset, tags, osm_nodes, st_asbinary(geom) FROM osm_ways WHERE id = ?";

  public static final String INSERT_WAY =
      "INSERT INTO osm_ways (id, version, uid, timestamp, changeset, tags, nodes, geom) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

  public static final String UPDATE_WAY =
      "UPDATE osm_ways SET version = ?, uid = ?, timestamp = ?, changeset = ?, tags = ?, nodes = ?, geom = ? WHERE id = ?";

  public static final String DELETE_WAY = "DELETE FROM osm_ways WHERE id = ?";

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

  public static void createExtensions(Connection connection) throws SQLException {
    connection.prepareStatement(CREATE_EXTENSION_HSTORE).execute();
    connection.prepareStatement(CREATE_EXTENSION_POSTGIS).execute();
  }

  public static void dropTables(Connection connection) throws SQLException {
    connection.prepareStatement(DROP_TABLE_INFO).execute();
    connection.prepareStatement(DROP_TABLE_USERS).execute();
    connection.prepareStatement(DROP_TABLE_NODES).execute();
    connection.prepareStatement(DROP_TABLE_WAYS).execute();
    connection.prepareStatement(DROP_TABLE_RELATIONS).execute();
  }

  public static void createTables(Connection connection) throws SQLException {
    connection.prepareStatement(CREATE_TABLE_INFO).execute();
    connection.prepareStatement(CREATE_TABLE_USERS).execute();
    connection.prepareStatement(CREATE_TABLE_NODES).execute();
    connection.prepareStatement(CREATE_TABLE_WAYS).execute();
    connection.prepareStatement(CREATE_TABLE_RELATIONS).execute();
  }
}
