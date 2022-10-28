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

package org.apache.baremaps.database.collection;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.collection.StoreException;
import org.locationtech.jts.geom.Coordinate;

/**
 * A read-only {@link LongDataMap} for coordinates baked by OpenStreetMap nodes stored in
 * PostgreSQL.
 */
public class PostgresCoordinateMap implements LongDataMap<Coordinate> {

  private static final String SELECT = "SELECT lon, lat FROM osm_nodes WHERE id = ?";

  private static final String SELECT_IN = "SELECT id, lon, lat FROM osm_nodes WHERE id = ANY (?)";

  private final DataSource dataSource;

  /** Constructs a {@link PostgresCoordinateMap}. */
  public PostgresCoordinateMap(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /** {@inheritDoc} */
  @Override
  public Coordinate get(long key) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, key);
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          double lon = result.getDouble(1);
          double lat = result.getDouble(2);
          return new Coordinate(lon, lat);
        } else {
          return null;
        }
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Coordinate> get(List<Long> keys) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      try (ResultSet result = statement.executeQuery()) {
        Map<Long, Coordinate> nodes = new HashMap<>();
        while (result.next()) {
          long key = result.getLong(1);
          double lon = result.getDouble(2);
          double lat = result.getDouble(3);
          nodes.put(key, new Coordinate(lon, lat));
        }
        return keys.stream().map(nodes::get).toList();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void put(long key, Coordinate value) {
    throw new UnsupportedOperationException();
  }
}
