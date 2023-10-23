/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.tilestore.postgres;


import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import javax.sql.DataSource;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.apache.baremaps.vectortile.tileset.Tileset;
import org.apache.baremaps.vectortile.tileset.TilesetQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read-only {@code TileStore} implementation that uses the PostgreSQL to generate vector tiles.
 * This {@code TileStore} combines the input queries, identifies common table expressions (CTE), and
 * generates a single optimized query that hits the database.
 */
public class PostgresTileStore implements TileStore {

  private static final Logger logger = LoggerFactory.getLogger(PostgresTileStore.class);

  private final DataSource datasource;

  private final Tileset tileset;

  public PostgresTileStore(DataSource datasource, Tileset tileset) {
    this.datasource = datasource;
    this.tileset = tileset;
  }

  protected boolean zoomPredicate(TilesetQuery query, int zoom) {
    return query.getMinzoom() <= zoom && zoom < query.getMaxzoom();
  }

  public String withQuery(TileCoord tileCoord) {
    var layers = tileset.getVectorLayers().stream()
        .map(layer -> Map.entry(layer.getId(), layer.getQueries().stream()
            .filter(
                query -> query.getMinzoom() <= tileCoord.z() && tileCoord.z() < query.getMaxzoom())
            .toList()))
        .filter(entry -> entry.getValue().size() > 0)
        .toList();

    var queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT (");

    for (int i = 0; i < layers.size(); i++) {
      var layer = layers.get(i);
      var layerId = layer.getKey();
      var layerQueries = layer.getValue().stream()
          .filter(layerQuery -> zoomPredicate(layerQuery, tileCoord.z())).toList();

      if (layerQueries.size() > 0) {
        if (i > 0) {
          queryBuilder.append(" || ");
        }

        var sqlBuilder = new StringBuilder();
        sqlBuilder.append("(WITH mvtgeom AS (\n");

        for (int j = 0; j < layerQueries.size(); j++) {
          if (j != 0) {
            sqlBuilder.append("UNION\n");
          }
          var layerQuery = layerQueries.get(j).getSql().replace(";", "");
          sqlBuilder.append(String.format("""
              SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(%d, %d, %d)) AS geom, t.tags, t.id
              FROM (%s) AS t
              WHERE t.geom && ST_TileEnvelope(%d, %d, %d, margin => (64.0/4096))
              """,
              tileCoord.z(), tileCoord.x(), tileCoord.y(),
              layerQuery,
              tileCoord.z(), tileCoord.x(), tileCoord.y()));
        }

        queryBuilder.append(sqlBuilder)
            .append(String.format(") SELECT ST_AsMVT(mvtgeom.*, '%s') FROM mvtgeom\n)", layerId));
      }
    }
    queryBuilder.append(") mvtTile");
    return queryBuilder.toString().replace("$zoom", String.valueOf(tileCoord.z()));
  }

  @Override
  public ByteBuffer read(TileCoord tileCoord) throws TileStoreException {
    String query = withQuery(tileCoord);

    logger.debug("Executing query: {}", query);

    long start = System.currentTimeMillis();

    try (Connection connection = datasource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        ByteArrayOutputStream data = new ByteArrayOutputStream()) {

      int length = 0;

      try (OutputStream gzip = new GZIPOutputStream(data)) {
        while (resultSet.next()) {
          byte[] bytes = resultSet.getBytes(1);
          length += bytes.length;
          gzip.write(bytes);
        }
      }

      long stop = System.currentTimeMillis();
      long duration = stop - start;

      // Log slow queries (> 10s)
      if (duration > 10_000) {
        logger.warn("Executed query for tile {} in {} ms: {}", tileCoord, duration, query);
      }

      if (length > 0) {
        return ByteBuffer.wrap(data.toByteArray());
      } else {
        return ByteBuffer.allocate(0);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      throw new TileStoreException(e);
    }
  }

  /**
   * This operation is not supported.
   */
  @Override
  public void write(TileCoord tileCoord, ByteBuffer blob) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }

  /**
   * This operation is not supported.
   */
  @Override
  public void delete(TileCoord tileCoord) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }

}
