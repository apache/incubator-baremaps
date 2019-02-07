package io.gazetteer.osm.postgis;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.sql.Connection;
import java.sql.SQLException;

public class PostgisSchema {

  public static final String CREATE_EXTENSION_HSTORE = "CREATE EXTENSION IF NOT EXISTS hstore";

  public static final String CREATE_EXTENSION_POSTGIS = "CREATE EXTENSION IF NOT EXISTS postgis";

  public static final String DROP_TABLE_INFO = "DROP TABLE IF EXISTS osm_info";

  public static final String DROP_TABLE_USERS = "DROP TABLE IF EXISTS osm_users";

  public static final String DROP_TABLE_RELATIONS = "DROP TABLE IF EXISTS osm_relations";

  public static final String CREATE_TABLE_INFO =
      "CREATE TABLE IF NOT EXISTS osm_info (" + "version integer NOT NULL" + ");";

  public static final String CREATE_TABLE_USERS =
      "CREATE TABLE IF NOT EXISTS osm_users (" + "id int NOT NULL," + "name text NOT NULL" + ");";

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

  public static final String CREATE_INDEX_RELATIONS =
      "CREATE INDEX IF NOT EXISTS osm_relations_idx ON osm_relations USING gist(geom)";

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
    connection.prepareStatement(NodeTable.DROP_TABLE).execute();
    connection.prepareStatement(WayTable.DROP_TABLE).execute();
    connection.prepareStatement(DROP_TABLE_RELATIONS).execute();
  }

  public static void createTables(Connection connection) throws SQLException {
    connection.prepareStatement(CREATE_TABLE_INFO).execute();
    connection.prepareStatement(CREATE_TABLE_USERS).execute();
    connection.prepareStatement(NodeTable.CREATE_TABLE).execute();
    connection.prepareStatement(WayTable.CREATE_TABLE).execute();
    connection.prepareStatement(CREATE_TABLE_RELATIONS).execute();
  }
}
