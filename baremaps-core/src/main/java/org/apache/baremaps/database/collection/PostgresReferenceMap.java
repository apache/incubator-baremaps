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



import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.collection.StoreException;

/**
 * A read-only {@code LongDataMap} for references baked by OpenStreetMap ways stored in Postgres.
 */
public class PostgresReferenceMap implements LongDataMap<List<Long>> {

  private static final String SELECT = "SELECT nodes FROM osm_ways WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, nodes FROM osm_ways WHERE id WHERE id = ANY (?)";

  private final DataSource dataSource;

  /** Constructs a {@code PostgresReferenceMap}. */
  public PostgresReferenceMap(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /** {@inheritDoc} */
  @Override
  public List<Long> get(long key) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, key);
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          List<Long> nodes = new ArrayList<>();
          Array array = result.getArray(1);
          if (array != null) {
            nodes = Arrays.asList((Long[]) array.getArray());
          }
          return nodes;
        } else {
          throw new IllegalArgumentException();
        }
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<List<Long>> get(List<Long> keys) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      try (ResultSet result = statement.executeQuery()) {
        Map<Long, List<Long>> referenceMap = new HashMap<>();
        while (result.next()) {
          List<Long> nodes = new ArrayList<>();
          long key = result.getLong(1);
          Array array = result.getArray(2);
          if (array != null) {
            nodes = Arrays.asList((Long[]) array.getArray());
          }
          referenceMap.put(key, nodes);
        }
        return keys.stream().map(referenceMap::get).toList();
      }
    } catch (SQLException e) {
      throw new StoreException(e);
    }
  }

  @Override
  public void put(long key, List<Long> value) {
    throw new UnsupportedOperationException();
  }
}
