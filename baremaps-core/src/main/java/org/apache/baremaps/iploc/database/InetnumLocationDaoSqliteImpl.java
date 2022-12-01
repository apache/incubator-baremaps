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



import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.baremaps.iploc.data.InetnumLocation;
import org.apache.baremaps.iploc.data.Ipv4Range;
import org.apache.baremaps.iploc.data.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/** Data access object for Sqlite JDBC to the inetnum_locations table */
public final class InetnumLocationDaoSqliteImpl implements InetnumLocationDao {

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

  private static final Logger logger = LoggerFactory.getLogger(InetnumLocationDaoSqliteImpl.class);

  private final HikariDataSource readDatasource;
  private final SQLiteDataSource writeDatasource;

  /**
   * Init the datasources
   *
   * @param url
   */
  public InetnumLocationDaoSqliteImpl(String url) {
    // Init the read datasource
    {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(url);
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      // config.setReadOnly(true);
      readDatasource = new HikariDataSource(config);
    }

    // Init the write datasource
    {
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

  /** {@inheritDoc} */
  @Override
  public Optional<InetnumLocation> findOne(long id) {
    return Optional.empty();
  }

  /** {@inheritDoc} */
  @Override
  public List<InetnumLocation> findAll() {
    List<InetnumLocation> results = new ArrayList<>();
    try (Connection connection = getReadConnection();
        PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
        ResultSet rs = stmt.executeQuery()) {
      // loop through the result set
      while (rs.next()) {
        results.add(new InetnumLocation(rs.getString("address"),
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
  @Override
  public List<InetnumLocation> findByIp(byte[] ip) {
    List<InetnumLocation> results = new ArrayList<>();
    try (Connection connection = getReadConnection();
        PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_BY_IP_SQL)) {
      stmt.setBytes(1, ip);
      stmt.setBytes(2, ip);
      try (ResultSet rs = stmt.executeQuery();) {
        while (rs.next()) {
          results.add(new InetnumLocation(rs.getString("address"),
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
  @Override
  public void save(InetnumLocation inetnumLocation) {
    try (Connection connection = getWriteConnection();
        PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {
      stmt.setString(1, inetnumLocation.getAddress());
      stmt.setBytes(2, inetnumLocation.getIpv4Range().getStart());
      stmt.setBytes(3, inetnumLocation.getIpv4Range().getEnd());
      stmt.setDouble(4, inetnumLocation.getLocation().getLatitude());
      stmt.setDouble(5, inetnumLocation.getLocation().getLongitude());
      stmt.setString(6, inetnumLocation.getNetwork());
      stmt.setString(7, inetnumLocation.getCountry());
      stmt.executeUpdate();
      logger.info(String.format("Data Added Successfully %s", inetnumLocation));
    } catch (SQLException e) {
      logger.error("Unable to save data", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void save(List<InetnumLocation> inetnumLocations) {
    try (Connection connection = getWriteConnection();
        PreparedStatement stmt = connection.prepareStatement(INSERT_SQL);) {
      connection.setAutoCommit(false);
      for (InetnumLocation inetnumLocation : inetnumLocations) {
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
      logger.info(String.format("Batch executed Successfully \n\t%s", inetnumLocations.stream()
          .map(InetnumLocation::toString).collect(Collectors.joining("\n\t"))));
    } catch (SQLException e) {
      logger.error("Unable to save data", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void update(InetnumLocation inetnumLocation, String[] params) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /** {@inheritDoc} */
  @Override
  public void delete(InetnumLocation inetnumLocation) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
