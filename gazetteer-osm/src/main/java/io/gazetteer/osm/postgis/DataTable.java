package io.gazetteer.osm.postgis;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataTable<K, V> {

  void createTable(Connection connection) throws SQLException;

  void createIndex(Connection connection) throws SQLException;

  void dropTable(Connection connection) throws SQLException;

  void dropIndex(Connection connection) throws SQLException;

  void insert(Connection connection, V entity) throws SQLException;

  void update(Connection connection, V entity) throws SQLException;

  V select(Connection connection, K id) throws SQLException;

  void delete(Connection connection, K id) throws SQLException;
}
