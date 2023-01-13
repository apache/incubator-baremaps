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



import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.apache.baremaps.collection.DataCollectionException;
import org.apache.baremaps.collection.DataMap;
import org.locationtech.jts.geom.Coordinate;

/**
 * A read-only {@link DataMap} for coordinates baked by OpenStreetMap nodes stored in PostgreSQL.
 */
public class PostgresCoordinateMap extends DataMap<Coordinate> {

  public static final String SELECT_CONTAINS_KEY = """
      SELECT 1
      FROM osm_nodes
      WHERE id = ? LIMIT 1""";

  public static final String SELECT_CONTAINS_VALUE = """
      SELECT 1
      FROM osm_nodes
      WHERE nodes = ? LIMIT 1""";

  private static final String SELECT_IN = """
      SELECT id, lon, lat
      FROM osm_nodes
      WHERE id
      WHERE id = ANY (?)""";

  private static final String SELECT_BY_ID = """
      SELECT lon, lat
      FROM osm_nodes
      WHERE id = ?""";

  private static final String SELECT_SIZE = """
      SELECT count()
      FROM osm_nodes
      """;

  private static final String SELECT_KEYS = """
      SELECT id
      FROM osm_nodes
      """;

  private static final String SELECT_VALUES = """
      SELECT lon, lat
      FROM osm_nodes
      """;

  private static final String SELECT_ENTRIES = """
      SELECT id, lon, lat
      FROM osm_nodes
      """;

  private final DataSource dataSource;

  /** Constructs a {@link PostgresCoordinateMap}. */
  public PostgresCoordinateMap(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /** {@inheritDoc} */
  @Override
  public Coordinate get(Object key) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
      statement.setLong(1, (Long) key);
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
      throw new DataCollectionException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Coordinate> getAll(List<Long> keys) {
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
      throw new DataCollectionException(e);
    }
  }

  @Override
  protected Iterator<Long> keyIterator() {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_KEYS)) {
      ResultSet result = statement.executeQuery();
      return new PostgresIterator<>(result, this::key);
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  private Long key(ResultSet resultSet) {
    try {
      return resultSet.getLong(1);
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  @Override
  protected Iterator<Coordinate> valueIterator() {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_VALUES)) {
      ResultSet result = statement.executeQuery();
      return new PostgresIterator<>(result, this::value);
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  private Coordinate value(ResultSet resultSet) {
    try {
      double lon = resultSet.getDouble(1);
      double lat = resultSet.getDouble(2);
      return new Coordinate(lon, lat);
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  @Override
  protected Iterator<Entry<Long, Coordinate>> entryIterator() {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_ENTRIES)) {
      ResultSet result = statement.executeQuery();
      return new PostgresIterator<>(result, this::entry);
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  private Entry<Long, Coordinate> entry(ResultSet resultSet) {
    try {
      long key = resultSet.getLong(1);
      double lon = resultSet.getDouble(1);
      double lat = resultSet.getDouble(2);
      return Map.entry(key, new Coordinate(lon, lat));
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  @Override
  public long sizeAsLong() {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_SIZE)) {
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          return result.getLong(1);
        } else {
          throw new DataCollectionException();
        }
      }
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  @Override
  public boolean containsKey(Object key) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_CONTAINS_KEY)) {
      statement.setLong(1, (Long) key);
      try (ResultSet result = statement.executeQuery()) {
        return result.next();
      }
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  @Override
  public boolean containsValue(Object value) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_CONTAINS_VALUE)) {
      statement.setArray(1, connection.createArrayOf("int8", (Long[]) value));
      try (ResultSet result = statement.executeQuery()) {
        return result.next();
      }
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  @Override
  public Coordinate put(Long key, Coordinate value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Coordinate remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {

  }
}
