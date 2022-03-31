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

package com.baremaps.iploc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Data access object for Sqlite JDBC to the inetnum_locations table */
public class InetnumLocationDaoImpl implements InetnumLocationDao {

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

  private static final Logger logger = LoggerFactory.getLogger(InetnumLocationDaoImpl.class);

  Connection connection = null;
  PreparedStatement stmt = null;
  String url;

  public InetnumLocationDaoImpl(String url) {
    this.url = url;
  }

  /**
   * Create a connection to the database file
   *
   * @return
   * @throws SQLException
   */
  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url);
  }

  /**
   * Get the element with the given id
   *
   * @param id
   * @return
   */
  @Override
  public Optional<InetnumLocation> get(long id) {
    return Optional.empty();
  }

  /**
   * Get all of the elements
   *
   * @return
   */
  @Override
  public List<InetnumLocation> getAll() {
    List<InetnumLocation> results = new ArrayList<>();
    try {
      connection = getConnection();
      stmt = connection.prepareStatement(SELECT_ALL_SQL);
      ResultSet rs = stmt.executeQuery();

      // loop through the result set
      while (rs.next()) {
        results.add(
            new InetnumLocation(
                rs.getString("name"),
                new Ipv4Range(rs.getBytes("ip_start"), rs.getBytes("ip_end")),
                rs.getDouble("latitude"),
                rs.getDouble("longitude")));
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
  public List<InetnumLocation> getAllByIp(byte[] ip) {
    List<InetnumLocation> results = new ArrayList<>();
    try {
      connection = getConnection();
      stmt = connection.prepareStatement(SELECT_ALL_BY_IP_SQL);
      stmt.setBytes(1, ip);
      stmt.setBytes(2, ip);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        results.add(
            new InetnumLocation(
                rs.getString("name"),
                new Ipv4Range(rs.getBytes("ip_start"), rs.getBytes("ip_end")),
                rs.getDouble("latitude"),
                rs.getDouble("longitude")));
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
    try {
      connection = getConnection();
      stmt = connection.prepareStatement(INSERT_SQL);
      stmt.setString(1, inetnumLocation.getName());
      stmt.setBytes(2, inetnumLocation.getIpv4Range().start());
      stmt.setBytes(3, inetnumLocation.getIpv4Range().end());
      stmt.setDouble(4, inetnumLocation.getLatitude());
      stmt.setDouble(5, inetnumLocation.getLongitude());
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
