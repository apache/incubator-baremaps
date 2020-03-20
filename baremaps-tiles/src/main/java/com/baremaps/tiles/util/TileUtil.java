/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.tiles.util;

import com.baremaps.core.stream.BatchSpliterator;
import com.baremaps.tiles.Tile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

public class TileUtil {

  public static final String BBOX = "SELECT st_asewkb(st_transform(st_setsrid(st_extent(geom), 3857), 4326)) as table_extent FROM osm_nodes;";

  public static Geometry bbox(Connection connection) throws SQLException, ParseException {
    try (PreparedStatement statement = connection.prepareStatement(BBOX)) {
      ResultSet result = statement.executeQuery();
      if (result.next()) {
        return new WKBReader().read(result.getBytes(1));
      } else {
        return null;
      }
    }
  }

  public static Stream<Tile> getTiles(Geometry geometry, int minZ, int maxZ) {
    Envelope envelope = geometry.getEnvelopeInternal();
    return StreamSupport.stream(new BatchSpliterator<>(IntStream.rangeClosed(minZ, maxZ).mapToObj(z -> z).flatMap(z -> {
      Tile min = getTiles(envelope.getMinX(), envelope.getMaxY(), z);
      Tile max = getTiles(envelope.getMaxX(), envelope.getMinY(), z);
      return IntStream.rangeClosed(min.getX(), max.getX()).mapToObj(i -> i)
          .flatMap(x -> IntStream.rangeClosed(min.getY(), max.getY()).mapToObj(i -> i).map(y -> new Tile(x, y, z)));
    }).spliterator(), 10), true);
  }

  public static Tile getTiles(double lon, double lat, int z) {
    int x = (int) ((lon + 180.0) / 360.0 * (1 << z));
    int y = (int) ((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat)))
        / Math.PI) / 2.0 * (1 << z));
    return new Tile(x, y, z);
  }

}