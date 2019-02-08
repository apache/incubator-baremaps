package io.gazetteer.osm.postgis;

import java.sql.Connection;
import java.sql.SQLException;

public interface PostgisTable<K, V> {

  void insert(Connection connection, V entity) throws SQLException;

  void update(Connection connection, V entity) throws SQLException;

  V select(Connection connection, K id) throws SQLException;

  void delete(Connection connection, K id) throws SQLException;
}
