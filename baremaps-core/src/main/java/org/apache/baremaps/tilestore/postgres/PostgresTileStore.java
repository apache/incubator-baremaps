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
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;
import javax.sql.DataSource;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.apache.baremaps.vectortile.tileset.Tileset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read-only {@code TileStore} implementation that uses the PostgreSQL to generate vector tiles.
 * This {@code TileStore} combines the input queries, identifies common table expressions (CTE), and
 * generates a single optimized sql that hits the database.
 */
public class PostgresTileStore implements TileStore {

  private static final Logger logger = LoggerFactory.getLogger(PostgresTileStore.class);

  private final DataSource datasource;

  private final Tileset tileset;

  /**
   * Constructs a {@code PostgresTileStore}.
   *
   * @param datasource the datasource
   * @param tileset the tileset
   */
  public PostgresTileStore(DataSource datasource, Tileset tileset) {
    this.datasource = datasource;
    this.tileset = tileset;
  }

  /**
   * A cache of queries.
   */
  private Map<Integer, Query> cache = new ConcurrentHashMap<>();

  /**
   * A record that holds the sql of a prepared statement and the number of parameters.
   * 
   * @param sql
   * @param parameters
   */
  protected record Query(String sql, int parameters) {
  }

  @Override
  public ByteBuffer read(TileCoord tileCoord) throws TileStoreException {
    var start = System.currentTimeMillis();

    // Prepare and cache the query
    var query = cache.computeIfAbsent(tileCoord.z(), z -> prepareQuery(tileset, z));

    // Fetch and compress the tile data
    try (var connection = datasource.getConnection();
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        var statement = connection.prepareStatement(query.sql())) {

      // Set the parameters for the tile
      for (int i = 0; i < query.parameters(); i += 3) {
        statement.setInt(i + 1, tileCoord.z());
        statement.setInt(i + 2, tileCoord.x());
        statement.setInt(i + 3, tileCoord.y());
      }

      // Log the sql query
      logger.debug("Executing sql for tile {}: {}", tileCoord, statement);

      try (ResultSet resultSet = statement.executeQuery();
          OutputStream gzip = new GZIPOutputStream(data)) {
        while (resultSet.next()) {
          byte[] bytes = resultSet.getBytes(1);
          gzip.write(bytes);
        }
      } catch (Exception e) {
        throw new TileStoreException(String.format("Failed to execute statement: %s", statement),
            e);
      }

      // Log slow queries (> 10s)
      long stop = System.currentTimeMillis();
      long duration = stop - start;
      if (duration > 10_000) {
        logger.warn("Executed sql for tile {} in {} ms", tileCoord, duration);
      }

      return ByteBuffer.wrap(data.toByteArray());

    } catch (Exception e) {
      throw new TileStoreException(e);
    }
  }

  /**
   * Prepare the sql query for a given tileset and zoom level.
   *
   * @param tileset the tileset
   * @param zoom the zoom level
   * @return
   */
  protected static Query prepareQuery(Tileset tileset, int zoom) {
    // Initialize a builder for the tile sql
    var tileSql = new StringBuilder();
    tileSql.append("SELECT ");

    // Iterate over the layers and keep track of the number of layers and parameters included in the
    // final sql
    var layers = tileset.getVectorLayers();
    var layerCount = 0;
    var paramCount = 0;
    for (var layer : layers) {

      // Initialize a builder for the layer sql
      var layerSql = new StringBuilder();
      var layerHead = String.format("(SELECT ST_AsMVT(mvtGeom.*, '%s') FROM (", layer.getId());
      layerSql.append(layerHead);

      // Iterate over the queries and keep track of the number of queries included in the final
      // sql
      var queries = layer.getQueries();
      var queryCount = 0;
      for (var query : queries) {

        // Only include the sql if the zoom level is in the range
        if (query.getMinzoom() <= zoom && zoom < query.getMaxzoom()) {

          // Add a union between queries
          if (queryCount > 0) {
            layerSql.append("UNION ALL ");
          }

          // Add the sql to the layer sql
          var querySql = query.getSql().trim()
              .replaceAll("\\s+", " ")
              .replace(";", "")
              .replace("?", "??")
              .replace("$zoom", String.valueOf(zoom));
          var querySqlWithParams = String.format(
              "SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(?, ?, ?)) AS geom, t.tags - 'id' AS tags, t.id AS id " +
                  "FROM (%s) AS t WHERE t.geom IS NOT NULL AND t.geom && ST_TileEnvelope(?, ?, ?, margin => (64.0/4096))",
              querySql);
          layerSql.append(querySqlWithParams);

          // Increase the parameter count (e.g. ?) and sql count
          paramCount += 6;
          queryCount++;
        }
      }

      // Add the tail of the layer sql
      var layerQueryTail = ") AS mvtGeom)";
      layerSql.append(layerQueryTail);

      // Only include the layer sql if queries were included for this layer
      if (queryCount > 0) {

        // Add the concatenation between layer queries
        if (layerCount > 0) {
          tileSql.append(" || ");
        }

        // Add the layer sql to the mvt sql
        tileSql.append(layerSql);

        // Increase the layer count
        layerCount++;
      }
    }

    // Add the tail of the tile sql
    var tileQueryTail = " AS mvtTile";
    tileSql.append(tileQueryTail);

    // Format the sql query
    var sql = tileSql.toString().replace("\n", " ");

    return new Query(sql, paramCount);
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

  @Override
  public void close() throws Exception {
    // do nothing
  }
}
