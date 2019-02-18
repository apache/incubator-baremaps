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

  public static final String DROP_TABLE_NODES = "DROP TABLE IF EXISTS osm_nodes";

  public static final String DROP_TABLE_WAYS = "DROP TABLE IF EXISTS osm_ways";

  public static final String DROP_TABLE_RELATIONS = "DROP TABLE IF EXISTS osm_relations";

  public static final String CREATE_TABLE_INFO =
      "CREATE TABLE IF NOT EXISTS osm_info (version integer NOT NULL);";

  public static final String CREATE_TABLE_USERS =
      "CREATE TABLE IF NOT EXISTS osm_users (id int NOT NULL, name text NOT NULL);";

  public static final String CREATE_TABLE_NODES =
      "CREATE TABLE IF NOT EXISTS osm_nodes ("
          + "id bigint NOT NULL,"
          + "version int NOT NULL,"
          + "uid int NOT NULL,"
          + "timestamp timestamp without time zone NOT NULL,"
          + "changeset bigint NOT NULL,"
          + "tags hstore,"
          + "geom geometry(point)"
          + ")";

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
          + "member_types text[],"
          + "member_roles text[],"
          + "geom geometry"
          + ")";

  public static final String UPDATE_RELATIONS_GEOMETRY_COLUMN =
      "UPDATE osm_relations SET geom = r2.geom"
          + "FROM ("
          + "SELECT r3.id as id, st_buildarea(st_makevalid(st_collect(w.geom))) as geom"
          + "FROM ("
          + "SELECT r4.id as id, unnest(r4.member_refs) as m"
          + "FROM osm_relations r4"
          + "WHERE r4.tags -> 'type' = 'multipolygon'"
          + ") r3 JOIN osm_ways w ON r3.m = w.id"
          + "GROUP BY r3.id"
          + ") r2"
          + "WHERE osm_relations.id = r2.id";

  public static final String DROP_INDEX_NODES = "DROP INDEX IF EXISTS osm_nodes_idx";

  public static final String DROP_INDEX_WAYS = "DROP INDEX IF EXISTS osm_ways_idx";

  public static final String DROP_INDEX_RELATIONS = "DROP INDEX IF EXISTS osm_relations_idx";

  public static final String CREATE_INDEX_NODES =
      "CREATE INDEX IF NOT EXISTS osm_nodes_idx ON osm_nodes USING gist(geom)";

  public static final String CREATE_INDEX_WAYS =
      "CREATE INDEX IF NOT EXISTS osm_ways_idx ON osm_ways USING gist(geom)";

  public static final String CREATE_INDEX_RELATIONS =
      "CREATE INDEX IF NOT EXISTS osm_relations_idx ON osm_relations USING gist(geom)";

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

  public static void updateGeometryColumns(Connection connection) throws SQLException {
    connection.prepareStatement(UPDATE_RELATIONS_GEOMETRY_COLUMN).execute();
  }

  public static void dropIndices(Connection connection) throws SQLException {
    connection.prepareStatement(DROP_INDEX_NODES).execute();
    connection.prepareStatement(DROP_INDEX_WAYS).execute();
    connection.prepareStatement(DROP_INDEX_RELATIONS).execute();
  }

  public static void createIndices(Connection connection) throws SQLException {
    connection.prepareStatement(CREATE_INDEX_NODES).execute();
    connection.prepareStatement(CREATE_INDEX_WAYS).execute();
    connection.prepareStatement(CREATE_INDEX_RELATIONS).execute();
  }
}
