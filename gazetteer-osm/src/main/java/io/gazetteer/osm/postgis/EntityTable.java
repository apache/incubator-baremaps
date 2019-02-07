package io.gazetteer.osm.postgis;

import io.gazetteer.osm.model.Entity;

import java.sql.Connection;
import java.sql.SQLException;

public interface EntityTable<E extends Entity> {

  void createTable(Connection connection) throws SQLException;

  void createIndex(Connection connection) throws SQLException;

  void dropTable(Connection connection) throws SQLException;

  void insert(Connection connection, E entity) throws SQLException;

  void update(Connection connection, E entity) throws SQLException;

  E select(Connection connection, long id) throws SQLException;

  void delete(Connection connection, long id) throws SQLException;
}
