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

package com.baremaps.osm.postgres;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheException;
import com.baremaps.osm.cache.CoordinateCache;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;

public class PostgresCoordinateCache implements CoordinateCache {

  private static final String SELECT =
      "SELECT lon, lat FROM osm_nodes WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, lon, lat FROM osm_nodes WHERE id = ANY (?)";

  private final DataSource dataSource;

  public PostgresCoordinateCache(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Coordinate get(Long id) throws CacheException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        double lon = result.getDouble(1);
        double lat = result.getDouble(2);
        return new Coordinate(lon, lat);
      } else {
        return null;
      }
    } catch (SQLException e) {
      throw new CacheException(e);
    }
  }

  @Override
  public List<Coordinate> get(List<Long> keys) throws CacheException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Coordinate> nodes = new HashMap<>();
      while (result.next()) {
        long id = result.getLong(1);
        double lon = result.getDouble(2);
        double lat = result.getDouble(3);
        nodes.put(id, new Coordinate(lon, lat));
      }
      return keys.stream().map(key -> nodes.get(key)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new CacheException(e);
    }
  }

  @Override
  public void add(Long key, Coordinate values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(List<Entry<Long, Coordinate>> storeEntries) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Long key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(List<Long> keys) {
    throw new UnsupportedOperationException();
  }

}
