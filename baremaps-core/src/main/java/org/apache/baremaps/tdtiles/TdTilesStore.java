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

package org.apache.baremaps.tdtiles;


import org.apache.baremaps.database.tile.*;
import org.apache.baremaps.openstreetmap.utils.GeometryUtils;
import org.apache.baremaps.tdtiles.building.Building;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * A read-only {@code TileStore} implementation that uses the PostgreSQL to generate 3d tiles.
 */
public class TdTilesStore {

  private static final Logger logger = LoggerFactory.getLogger(TdTilesStore.class);
  private static final String QUERY =
      "select st_asbinary(geom), tags -> 'buildings:height', tags -> 'height', tags -> 'buildings:levels'  from osm_ways where tags ? 'building' and st_intersects( st_force3d(geom,0), st_makeenvelope(%1$s, %2$s, %3$s, %4$s, 4326)) LIMIT %5$s";


  private final DataSource datasource;

  public TdTilesStore(DataSource datasource) {
    this.datasource = datasource;
  }

  public List<Building> read(float xmin, float xmax, float ymin, float ymax, int limit) throws TileStoreException {
    try (Connection connection = datasource.getConnection();
        Statement statement = connection.createStatement()) {

      String sql = String.format(QUERY, ymin * 180 / (float)Math.PI, xmin * 180 / (float)Math.PI, ymax * 180 / (float)Math.PI, xmax * 180 / (float)Math.PI, limit);
      logger.debug("Executing query: {}", sql);

      List<Building> buildings = new ArrayList<>();

      try (ResultSet resultSet = statement.executeQuery(sql)) {
        while (resultSet.next()) {
          byte[] bytes = resultSet.getBytes(1);
          Geometry geometry = GeometryUtils.deserialize(bytes);

          String buildingHeight = resultSet.getString(2);
          String height = resultSet.getString(3);
          String buildingLevels = resultSet.getString(4);
          float finalHeight = 10;
          if(buildingHeight != null) {
            finalHeight = Float.parseFloat(buildingHeight.replaceAll("[^0-9]", ""));
          } else if(height != null) {
            finalHeight = Float.parseFloat(height.replaceAll("[^0-9]", ""));
          } else if(buildingLevels != null) {
            finalHeight = Float.parseFloat(buildingLevels.replaceAll("[^0-9]", "")) * 3;
          }

          buildings.add(new Building(geometry, finalHeight));
        }
      }
      return buildings;
    } catch (SQLException e) {
      throw new TileStoreException(e);
    }
  }

}
