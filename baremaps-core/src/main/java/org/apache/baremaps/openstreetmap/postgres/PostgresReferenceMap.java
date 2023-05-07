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

package org.apache.baremaps.openstreetmap.postgres;



import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.sql.DataSource;
import org.apache.baremaps.collection.DataCollectionException;
import org.apache.baremaps.collection.DataMap;

/**
 * A read-only {@code LongDataMap} for references baked by OpenStreetMap ways stored in Postgres.
 */
public class PostgresReferenceMap extends DataMap<List<Long>> {

  public static final String SELECT_CONTAINS_KEY = """
      SELECT 1
      FROM osm_ways
      WHERE id = ? LIMIT 1""";

  public static final String SELECT_CONTAINS_VALUE = """
      SELECT 1
      FROM osm_ways
      WHERE nodes = ? LIMIT 1""";

  private static final String SELECT_IN = """
      SELECT id, nodes
      FROM osm_ways
      WHERE id
      WHERE id = ANY (?)""";

  private static final String SELECT_BY_ID = """
      SELECT nodes
      FROM osm_ways
      WHERE id = ?""";

  private static final String SELECT_SIZE = """
      SELECT count()
      FROM osm_ways
      """;

  private static final String SELECT_KEYS = """
      SELECT id
      FROM osm_ways
      """;

  private static final String SELECT_VALUES = """
      SELECT nodes
      FROM osm_ways
      """;

  private static final String SELECT_ENTRIES = """
      SELECT id, nodes
      FROM osm_ways
      """;

  private final DataSource dataSource;

  /**
   * Constructs a {@code PostgresReferenceMap}.
   */
  public PostgresReferenceMap(DataSource dataSource) {
    this.dataSource = dataSource;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Long> get(Object key) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
      statement.setLong(1, (Long) key);
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          Array array = result.getArray(1);
          return array == null ? Collections.emptyList() : Arrays.asList((Long[]) array.getArray());
        } else {
          throw new IllegalArgumentException();
        }
      }
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<List<Long>> getAll(List<Long> keys) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      try (ResultSet result = statement.executeQuery()) {
        Map<Long, List<Long>> referenceMap = new HashMap<>();
        while (result.next()) {
          long key = result.getLong(1);
          Array array = result.getArray(2);
          referenceMap.put(key,
              array == null ? List.of() : Arrays.asList((Long[]) array.getArray()));
        }
        return keys.stream().map(referenceMap::get).toList();
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
  protected Iterator<List<Long>> valueIterator() {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_VALUES)) {
      ResultSet result = statement.executeQuery();
      return new PostgresIterator<>(result, this::value);
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  private List<Long> value(ResultSet resultSet) {
    try {
      Array array = resultSet.getArray(1);
      return array == null ? List.of() : Arrays.asList((Long[]) array.getArray());
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  @Override
  protected Iterator<Entry<Long, List<Long>>> entryIterator() {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_ENTRIES)) {
      ResultSet result = statement.executeQuery();
      return new PostgresIterator<>(result, this::entry);
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  private Entry<Long, List<Long>> entry(ResultSet resultSet) {
    try {
      long key = resultSet.getLong(1);
      Array array = resultSet.getArray(2);
      List<Long> value = array == null ? List.of() : Arrays.asList((Long[]) array.getArray());
      return Map.entry(key, value);
    } catch (SQLException e) {
      throw new DataCollectionException(e);
    }
  }

  @Override
  public List<Long> put(Long key, List<Long> value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Long> remove(Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
}
