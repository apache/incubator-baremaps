/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.iploc;



import static org.apache.baremaps.iploc.InetAddressUtils.fromByteArray;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.baremaps.stream.StreamUtils;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A repository for {@link IpLocObject} objects.
 */
public final class IpLocRepository {

  private static final String DROP_TABLE = """
      DROP TABLE IF EXISTS inetnum_locations""";

  private static final String CREATE_TABLE = """
      CREATE TABLE IF NOT EXISTS inetnum_locations (
          id integer PRIMARY KEY,
          geocoder_input text,
          ip_start blob,
          ip_end blob,
          longitude real,
          latitude real,
          network text,
          country text,
          source text,
          precision text
      )""";

  private static final String CREATE_INDEX = """
      CREATE INDEX inetnum_locations_ips ON inetnum_locations (ip_start,ip_end);""";

  private static final String INSERT_SQL =
      """
          INSERT INTO inetnum_locations(geocoder_input, ip_start, ip_end, longitude, latitude, network, country, source, precision)
          VALUES(?,?,?,?,?,?,?,?,?)""";

  private static final String SELECT_ALL_SQL =
      """
          SELECT id, geocoder_input, ip_start, ip_end, longitude, latitude, network, country, source, precision
          FROM inetnum_locations;""";

  private static final String SELECT_ALL_BY_IP_SQL =
      """
          SELECT id, geocoder_input, ip_start, ip_end, longitude, latitude, network, country, source, precision
          FROM inetnum_locations
          WHERE ip_start <= ? AND ip_end >= ?
          ORDER BY ip_start DESC, ip_end ASC;""";

  private static final Logger logger = LoggerFactory.getLogger(IpLocRepository.class);

  private final DataSource dataSource;

  /**
   * Constructs an {@code IpLocRepository} with the specified {@code DataSource}.
   *
   * @param dataSource the data source
   */
  public IpLocRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Drops the table.
   */
  public void dropTable() {
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(DROP_TABLE)) {
      statement.execute();
    } catch (SQLException e) {
      logger.error("Unable to drop inetnum locations table", e);
    }
  }

  /**
   * Creates the table.
   */
  public void createTable() {
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(CREATE_TABLE)) {
      statement.execute();
    } catch (SQLException e) {
      logger.error("Unable to create inetnum locations table", e);
    }
  }

  /**
   * Creates the index.
   */
  public void createIndex() {
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(CREATE_INDEX)) {
      statement.execute();
    } catch (SQLException e) {
      logger.error("Unable to create inetnum locations index", e);
    }
  }

  /**
   * Returns all the {@code IpLocObject} objects in the repository.
   *
   * @return the list of {@code IpLocObject} objects
   */
  public List<IpLocObject> findAll() {
    var results = new ArrayList<IpLocObject>();
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(SELECT_ALL_SQL);
        ResultSet resultSet = statement.executeQuery()) {
      // loop through the result set
      while (resultSet.next()) {
        results.add(resultSetToPojo(resultSet));
      }
    } catch (SQLException e) {
      logger.error("Unable to select inetnum locations", e);
    }
    return results;
  }

  private IpLocObject resultSetToPojo(ResultSet resultSet) throws SQLException {
    return new IpLocObject(
        resultSet.getString("geocoder_input"),
        new InetRange(
            fromByteArray(resultSet.getBytes("ip_start")),
            fromByteArray(resultSet.getBytes("ip_end"))),
        new Coordinate(
            resultSet.getDouble("longitude"),
            resultSet.getDouble("latitude")),
        resultSet.getString("network"),
        resultSet.getString("country"),
        resultSet.getString("source"),
        IpLocPrecision.valueOf(resultSet.getString("precision")));
  }

  /**
   * Returns the {@code IpLocObject} objects in the repository that contain the specified IP.
   *
   * @param inetAddress the IP address
   * @return the list of {@code IpLocObject} objects
   */
  public List<IpLocObject> findByInetAddress(InetAddress inetAddress) {
    var ipLocObjects = new ArrayList<IpLocObject>();
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(SELECT_ALL_BY_IP_SQL)) {
      statement.setBytes(1, inetAddress.getAddress());
      statement.setBytes(2, inetAddress.getAddress());
      try (var resultSet = statement.executeQuery();) {
        while (resultSet.next()) {
          ipLocObjects.add(resultSetToPojo(resultSet));
        }
      }
    } catch (SQLException e) {
      logger.error("Unable to select inetnum locations", e);
    }
    return ipLocObjects;
  }

  /**
   * Saves the {@code IpLocObject} objects in the repository.
   *
   * @param ipLocObjects the list of {@code IpLocObject} objects
   */
  public void save(List<IpLocObject> ipLocObjects) {
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(INSERT_SQL);) {
      connection.setAutoCommit(false);
      for (IpLocObject ipLocObject : ipLocObjects) {
        statement.setString(1, ipLocObject.geocoderInput());
        statement.setBytes(2, ipLocObject.inetRange().start().getAddress());
        statement.setBytes(3, ipLocObject.inetRange().end().getAddress());
        statement.setDouble(4, ipLocObject.coordinate().getX());
        statement.setDouble(5, ipLocObject.coordinate().getY());
        statement.setString(6, ipLocObject.network());
        statement.setString(7, ipLocObject.country());
        statement.setString(8, ipLocObject.source());
        statement.setString(9, ipLocObject.precision().toString());
        statement.addBatch();
      }
      statement.executeBatch();
      connection.commit();
    } catch (SQLException e) {
      logger.error("Unable to save data", e);
    }
  }

  /**
   * Saves the {@code IpLocObject} objects in the repository.
   *
   * @param ipLocObjects the stream of {@code IpLocObject} objects
   */
  public void save(Stream<IpLocObject> ipLocObjects) {
    StreamUtils.partition(ipLocObjects, 100)
        .map(Stream::toList)
        .forEach(this::save);
  }
}
