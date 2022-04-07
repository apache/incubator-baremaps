/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.iploc.database;

import com.baremaps.iploc.data.InetnumLocation;
import com.baremaps.iploc.data.Ipv4Range;
import com.baremaps.iploc.data.Location;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/** Data access object for Sqlite JDBC to the inetnum_locations table */
public final class InetnumLocationDaoSqliteImpl implements InetnumLocationDao {

  private static final String INSERT_SQL =
      "INSERT INTO inetnum_locations(name, ip_start, ip_end, latitude, longitude) VALUES(?,?,?,?,?)";
  private static final String SELECT_ALL_SQL =
      "SELECT "
          + "id, \n"
          + "name, \n"
          + "ip_start, \n"
          + "ip_end, \n"
          + "latitude, \n"
          + "longitude FROM inetnum_locations;";
  private static final String SELECT_ALL_BY_IP_SQL =
      "SELECT "
          + "id, \n"
          + "name, \n"
          + "ip_start, \n"
          + "ip_end, \n"
          + "latitude, \n"
          + "longitude FROM inetnum_locations WHERE ip_start <= ? AND ip_end >= ?;";

  private static final Logger logger = LoggerFactory.getLogger(InetnumLocationDaoSqliteImpl.class);

  private final HikariDataSource readDatasource;
  private final SQLiteDataSource writeDatasource;
  private final String url;

  /**
   * Init the datasources
   *
   * @param url
   */
  public InetnumLocationDaoSqliteImpl(String url) {
    this.url = url;

    {
      // Init the read datasource
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(url);
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      // config.setReadOnly(true);
      readDatasource = new HikariDataSource(config);
    }

    {
      // Init the write datasource
      SQLiteConfig config = new SQLiteConfig();
      writeDatasource = new SQLiteDataSource(config);
      writeDatasource.setUrl(url);
    }
  }

  /**
   * Get a connection to the database file in read only
   *
   * @return
   * @throws SQLException
   */
  public Connection getReadConnection() throws SQLException {
    return readDatasource.getConnection();
  }

  /**
   * Get a connection to the database file in read only
   *
   * @return
   * @throws SQLException
   */
  public Connection getWriteConnection() throws SQLException {
    return writeDatasource.getConnection();
  }

  /**
   * Get the element with the given id
   *
   * @param id
   * @return
   */
  @Override
  public Optional<InetnumLocation> findOne(long id) {
    return Optional.empty();
  }

  /**
   * Get all of the elements
   *
   * @return
   */
  @Override
  public List<InetnumLocation> findAll() {
    List<InetnumLocation> results = new ArrayList<>();
    Connection connection = null;
    PreparedStatement stmt = null;
    try {
      connection = getReadConnection();
      stmt = connection.prepareStatement(SELECT_ALL_SQL);
      ResultSet rs = stmt.executeQuery();

      // loop through the result set
      while (rs.next()) {
        results.add(
            new InetnumLocation(
                rs.getString("name"),
                new Ipv4Range(rs.getBytes("ip_start"), rs.getBytes("ip_end")),
                new Location(rs.getDouble("latitude"), rs.getDouble("longitude"))));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (stmt != null) stmt.close();
        if (connection != null) connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return results;
  }

  /**
   * Get all of the elements that have a range that contains the given IP
   *
   * @return
   */
  @Override
  public List<InetnumLocation> findByIp(byte[] ip) {
    List<InetnumLocation> results = new ArrayList<>();
    Connection connection = null;
    PreparedStatement stmt = null;
    try {
      connection = getReadConnection();
      stmt = connection.prepareStatement(SELECT_ALL_BY_IP_SQL);
      stmt.setBytes(1, ip);
      stmt.setBytes(2, ip);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        results.add(
            new InetnumLocation(
                rs.getString("name"),
                new Ipv4Range(rs.getBytes("ip_start"), rs.getBytes("ip_end")),
                new Location(rs.getDouble("latitude"), rs.getDouble("longitude"))));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (stmt != null) stmt.close();
        if (connection != null) connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return results;
  }

  /**
   * Insert the given element in the database
   *
   * @param inetnumLocation
   */
  @Override
  public void save(InetnumLocation inetnumLocation) {
    Connection connection = null;
    PreparedStatement stmt = null;
    try {
      connection = getWriteConnection();
      stmt = connection.prepareStatement(INSERT_SQL);
      stmt.setString(1, inetnumLocation.getName());
      stmt.setBytes(2, inetnumLocation.getIpv4Range().start());
      stmt.setBytes(3, inetnumLocation.getIpv4Range().end());
      stmt.setDouble(4, inetnumLocation.getLocation().getLatitude());
      stmt.setDouble(5, inetnumLocation.getLocation().getLongitude());
      stmt.executeUpdate();
      logger.debug("Data Added Successfully " + inetnumLocation);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (stmt != null) stmt.close();
        if (connection != null) connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Insert a batch of elements in the database
   *
   * @param inetnumLocations
   */
  @Override
  public void save(List<InetnumLocation> inetnumLocations) {
    Connection connection = null;
    PreparedStatement stmt = null;
    try {
      connection = getWriteConnection();
      connection.setAutoCommit(false);
      stmt = connection.prepareStatement(INSERT_SQL);
      for (InetnumLocation inetnumLocation : inetnumLocations) {
        stmt.setString(1, inetnumLocation.getName());
        stmt.setBytes(2, inetnumLocation.getIpv4Range().start());
        stmt.setBytes(3, inetnumLocation.getIpv4Range().end());
        stmt.setDouble(4, inetnumLocation.getLocation().getLatitude());
        stmt.setDouble(5, inetnumLocation.getLocation().getLongitude());
        stmt.addBatch();
      }
      stmt.executeBatch();
      connection.commit();
      logger.debug("Batch executed Successfully " + inetnumLocations);
    } catch (SQLException e) {
      e.printStackTrace();
      // connection.rollback();
    } finally {
      try {
        if (stmt != null) stmt.close();
        if (connection != null) connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Update the given element
   *
   * @param inetnumLocation
   * @param params
   */
  @Override
  public void update(InetnumLocation inetnumLocation, String[] params) {}

  /**
   * Delete the given element
   *
   * @param inetnumLocation
   */
  @Override
  public void delete(InetnumLocation inetnumLocation) {}
}
