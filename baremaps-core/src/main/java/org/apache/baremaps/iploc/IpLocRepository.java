/*
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

package org.apache.baremaps.iploc;



import static org.apache.baremaps.iploc.InetUtils.fromByteArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
          address text NOT NULL,
          ip_start blob,
          ip_end blob,
          longitude real,
          latitude real,
          network text,
          country text
      )""";

  private static final String CREATE_INDEX = """
      CREATE INDEX inetnum_locations_ips ON inetnum_locations (ip_start,ip_end);""";

  private static final String INSERT_SQL =
      """
          INSERT INTO inetnum_locations(address, ip_start, ip_end, longitude, latitude, network, country)
          VALUES(?,?,?,?,?,?,?)""";

  private static final String SELECT_ALL_SQL = """
      SELECT id, address, ip_start, ip_end, longitude, latitude, network, country
      FROM inetnum_locations;""";

  private static final String SELECT_ALL_BY_IP_SQL = """
      SELECT id, address, ip_start, ip_end, longitude, latitude, network, country
      FROM inetnum_locations
      WHERE ip_start <= ? AND ip_end >= ?
      ORDER BY ip_start DESC, ip_end ASC;""";

  private static final Logger logger = LoggerFactory.getLogger(IpLocRepository.class);

  private final DataSource dataSource;

  public IpLocRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void dropTable() {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(DROP_TABLE)) {
      stmt.execute();
    } catch (SQLException e) {
      logger.error("Unable to drop inetnum locations table", e);
    }
  }

  public void createTable() {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(CREATE_TABLE)) {
      stmt.execute();
    } catch (SQLException e) {
      logger.error("Unable to create inetnum locations table", e);
    }
  }

  public void createIndex() {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(CREATE_INDEX)) {
      stmt.execute();
    } catch (SQLException e) {
      logger.error("Unable to create inetnum locations index", e);
    }
  }

  /** {@inheritDoc} */
  public List<IpLocObject> findAll() {
    List<IpLocObject> results = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
        ResultSet rs = stmt.executeQuery()) {
      // loop through the result set
      while (rs.next()) {
        results.add(new IpLocObject(
            rs.getString("address"),
            fromByteArray(rs.getBytes("ip_start")),
            fromByteArray(rs.getBytes("ip_end")),
            new Coordinate(rs.getDouble("latitude"), rs.getDouble("longitude")),
            rs.getString("network"),
            rs.getString("country")));
      }
    } catch (SQLException e) {
      logger.error("Unable to select inetnum locations", e);
    }
    return results;
  }

  /** {@inheritDoc} */
  public List<IpLocObject> findByIp(byte[] ip) {
    List<IpLocObject> results = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_BY_IP_SQL)) {
      stmt.setBytes(1, ip);
      stmt.setBytes(2, ip);
      try (ResultSet rs = stmt.executeQuery();) {
        while (rs.next()) {
          results.add(new IpLocObject(
              rs.getString("address"),
              fromByteArray(rs.getBytes("ip_start")),
              fromByteArray(rs.getBytes("ip_end")),
              new Coordinate(rs.getDouble("latitude"), rs.getDouble("longitude")),
              rs.getString("network"),
              rs.getString("country")));
        }
      }
    } catch (SQLException e) {
      logger.error("Unable to select inetnum locations", e);
    }
    return results;
  }

  /** {@inheritDoc} */
  public void save(IpLocObject inetnumLocation) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {
      stmt.setString(1, inetnumLocation.address());
      stmt.setBytes(2, inetnumLocation.start().getAddress());
      stmt.setBytes(3, inetnumLocation.end().getAddress());
      stmt.setDouble(4, inetnumLocation.coordinate().getX());
      stmt.setDouble(5, inetnumLocation.coordinate().getY());
      stmt.setString(6, inetnumLocation.network());
      stmt.setString(7, inetnumLocation.country());
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Unable to save data", e);
    }
  }

  /** {@inheritDoc} */
  public void save(List<IpLocObject> inetnumLocations) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(INSERT_SQL);) {
      connection.setAutoCommit(false);
      for (IpLocObject inetnumLocation : inetnumLocations) {
        stmt.setString(1, inetnumLocation.address());
        stmt.setBytes(2, inetnumLocation.start().getAddress());
        stmt.setBytes(3, inetnumLocation.end().getAddress());
        stmt.setDouble(4, inetnumLocation.coordinate().getX());
        stmt.setDouble(5, inetnumLocation.coordinate().getY());
        stmt.setString(6, inetnumLocation.network());
        stmt.setString(7, inetnumLocation.country());
        stmt.addBatch();
      }
      stmt.executeBatch();
      connection.commit();
    } catch (SQLException e) {
      logger.error("Unable to save data", e);
    }
  }

  public void save(Stream<IpLocObject> ipLocStream) {
    StreamUtils.partition(ipLocStream, 100)
        .map(Stream::toList)
        .forEach(this::save);
  }
}
