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

package org.apache.baremaps.iploc.database;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.apache.baremaps.iploc.data.IpLoc;
import org.apache.baremaps.iploc.data.Ipv4Range;
import org.apache.baremaps.iploc.data.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A repository for {@link IpLoc} objects.
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
          latitude real,
          longitude real,
          network text,
          country text
      )""";

  private static final String CREATE_INDEX = """
      CREATE INDEX inetnum_locations_ips ON inetnum_locations (ip_start,ip_end);""";

  private static final String INSERT_SQL =
      """
          INSERT INTO inetnum_locations(address, ip_start, ip_end, latitude, longitude, network, country)
          VALUES(?,?,?,?,?,?,?)""";

  private static final String SELECT_ALL_SQL = """
      SELECT id, address, ip_start, ip_end, latitude, longitude, network, country
      FROM inetnum_locations;""";

  private static final String SELECT_ALL_BY_IP_SQL = """
      SELECT id, address, ip_start, ip_end, latitude, longitude, network, country
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
  public List<IpLoc> findAll() {
    List<IpLoc> results = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
        ResultSet rs = stmt.executeQuery()) {
      // loop through the result set
      while (rs.next()) {
        results.add(new IpLoc(rs.getString("address"),
            new Ipv4Range(rs.getBytes("ip_start"), rs.getBytes("ip_end")),
            new Location(rs.getDouble("latitude"), rs.getDouble("longitude")),
            rs.getString("network"), rs.getString("country")));
      }
    } catch (SQLException e) {
      logger.error("Unable to select inetnum locations", e);
    }
    return results;
  }

  /** {@inheritDoc} */
  public List<IpLoc> findByIp(byte[] ip) {
    List<IpLoc> results = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_BY_IP_SQL)) {
      stmt.setBytes(1, ip);
      stmt.setBytes(2, ip);
      try (ResultSet rs = stmt.executeQuery();) {
        while (rs.next()) {
          results.add(new IpLoc(rs.getString("address"),
              new Ipv4Range(rs.getBytes("ip_start"), rs.getBytes("ip_end")),
              new Location(rs.getDouble("latitude"), rs.getDouble("longitude")),
              rs.getString("network"), rs.getString("country")));
        }
      }
    } catch (SQLException e) {
      logger.error("Unable to select inetnum locations", e);
    }
    return results;
  }

  /** {@inheritDoc} */
  public void save(IpLoc inetnumLocation) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {
      stmt.setString(1, inetnumLocation.getAddress());
      stmt.setBytes(2, inetnumLocation.getIpv4Range().getStart());
      stmt.setBytes(3, inetnumLocation.getIpv4Range().getEnd());
      stmt.setDouble(4, inetnumLocation.getLocation().getLatitude());
      stmt.setDouble(5, inetnumLocation.getLocation().getLongitude());
      stmt.setString(6, inetnumLocation.getNetwork());
      stmt.setString(7, inetnumLocation.getCountry());
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Unable to save data", e);
    }
  }

  /** {@inheritDoc} */
  public void save(List<IpLoc> inetnumLocations) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(INSERT_SQL);) {
      connection.setAutoCommit(false);
      for (IpLoc inetnumLocation : inetnumLocations) {
        stmt.setString(1, inetnumLocation.getAddress());
        stmt.setBytes(2, inetnumLocation.getIpv4Range().getStart());
        stmt.setBytes(3, inetnumLocation.getIpv4Range().getEnd());
        stmt.setDouble(4, inetnumLocation.getLocation().getLatitude());
        stmt.setDouble(5, inetnumLocation.getLocation().getLongitude());
        stmt.setString(6, inetnumLocation.getNetwork());
        stmt.setString(7, inetnumLocation.getCountry());
        stmt.addBatch();
      }
      stmt.executeBatch();
      connection.commit();
    } catch (SQLException e) {
      logger.error("Unable to save data", e);
    }
  }
}
