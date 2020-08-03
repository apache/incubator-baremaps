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

package com.baremaps.osm.cache;

import com.baremaps.osm.geometry.GeometryUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.sql.DataSource;
import jnr.ffi.annotations.In;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

public class PostgisCoordinateCache implements Cache<Long, Coordinate> {

  private static final String SELECT =
      "SELECT st_asbinary(geom) FROM osm_nodes WHERE id = ?";

  private static final String SELECT_IN =
      "SELECT id, st_asbinary(geom) FROM osm_nodes WHERE id = ANY (?)";

  private final DataSource dataSource;

  @Inject
  public PostgisCoordinateCache(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Coordinate get(Long id) throws CacheException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT)) {
      statement.setLong(1, id);
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        Point point = (Point) GeometryUtil.deserialize(result.getBytes(6));
        return point.getCoordinate();
      } else {
        throw new IllegalArgumentException();
      }
    } catch (SQLException e) {
      throw new CacheException(e);
    }
  }

  @Override
  public List<Coordinate> getAll(List<Long> keys) throws CacheException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_IN)) {
      statement.setArray(1, connection.createArrayOf("int8", keys.toArray()));
      ResultSet result = statement.executeQuery();
      Map<Long, Coordinate> nodes = new HashMap<>();
      while (result.next()) {
        Long id = result.getLong(1);
        Point point = (Point) GeometryUtil.deserialize(result.getBytes(2));
        nodes.put(id, point.getCoordinate());
      }
      return keys.stream().map(key -> nodes.get(key)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new CacheException(e);
    }
  }

  @Override
  public void put(Long key, Coordinate values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(List<Entry<Long, Coordinate>> storeEntries) {
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
