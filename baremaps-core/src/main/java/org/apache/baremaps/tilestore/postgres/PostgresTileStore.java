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
import java.io.IOException;
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

  private Map<Integer, TileQuery> cache = new ConcurrentHashMap<>();

  @Override
  public ByteBuffer read(TileCoord tileCoord) throws TileStoreException {
    var query = cache.computeIfAbsent(tileCoord.z(), z -> prepareQuery(tileset, z));
    try (var connection = datasource.getConnection()) {
      return query.execute(connection, tileCoord);
    } catch (Exception e) {
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


  protected static TileQuery prepareQuery(Tileset tileset, int zoom) {
    // Initialize a builder for the tile query
    var tileQuery = new StringBuilder();
    tileQuery.append("SELECT (");

    // Iterate over the layers and keep track of the number of layers and parameters included in the
    // final query
    var layers = tileset.getVectorLayers();
    var layerCount = 0;
    var paramCount = 0;
    for (var layer : layers) {

      // Initialize a builder for the layer query
      var layerQuery = new StringBuilder();
      var layerHead = "(WITH mvtGeom AS (";
      layerQuery.append(layerHead);

      // Iterate over the queries and keep track of the number of queries included in the final
      // query
      var queries = layer.getQueries();
      var queryCount = 0;
      for (var query : queries) {

        // Only include the query if the zoom level is in the range
        if (query.getMinzoom() <= zoom && zoom < query.getMaxzoom()) {

          // Add a union between queries
          if (queryCount > 0) {
            layerQuery.append("UNION ");
          }

          // Add the query to the layer query
          var sql = query.getSql()
              .replace(";", "")
              .replace("?", "??")
              .replace("$zoom", String.valueOf(zoom));
          var queryWithParams = String.format(
              "SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(?, ?, ?)) AS geom, t.tags, t.id " +
                  "FROM (%s) AS t WHERE t.geom && ST_TileEnvelope(?, ?, ?, margin => (64.0/4096))",
              sql);
          layerQuery.append(queryWithParams);

          // Increase the parameter count (e.g. ?) and query count
          paramCount += 6;
          queryCount++;
        }
      }

      // Add the tail of the layer query
      var layerQueryTail =
          String.format(") SELECT ST_AsMVT(mvtGeom.*, '%s') FROM mvtGeom)", layer.getId());
      layerQuery.append(layerQueryTail);

      // Only include the layer query if queries were included for this layer
      if (queryCount > 0) {

        // Add the concatenation between layer queries
        if (layerCount > 0) {
          tileQuery.append(" || ");
        }

        // Add the layer query to the mvt query
        tileQuery.append(layerQuery);

        // Increase the layer count
        layerCount++;
      }
    }

    // Add the tail of the tile query
    var tileQueryTail = ") mvtTile";
    tileQuery.append(tileQueryTail);

    // Log the resulting query
    var query = tileQuery.toString().replace("\n", " ");
    logger.debug("query: {}", query);

    return new TileQuery(query, paramCount);
  }

  public record TileQuery(String query, int paramCount) {

    public ByteBuffer execute(Connection connection, TileCoord tileCoord)
        throws SQLException, IOException {
      long start = System.currentTimeMillis();
      try (var statement = connection.prepareStatement(query)) {

        // Set the parameters for the tile
        for (int i = 0; i < paramCount; i += 3) {
          statement.setInt(i + 1, tileCoord.z());
          statement.setInt(i + 2, tileCoord.x());
          statement.setInt(i + 3, tileCoord.y());
        }

        // Fetch and compress the tile data
        try (ByteArrayOutputStream data = new ByteArrayOutputStream();) {
          try (ResultSet resultSet = statement.executeQuery();
              OutputStream gzip = new GZIPOutputStream(data)) {
            while (resultSet.next()) {
              byte[] bytes = resultSet.getBytes(1);
              gzip.write(bytes);
            }
          }
          return ByteBuffer.wrap(data.toByteArray());

        } finally {
          // Log slow queries (> 10s)
          long stop = System.currentTimeMillis();
          long duration = stop - start;
          if (duration > 10_000) {
            logger.warn("Executed query for tile {} in {} ms", tileCoord, duration);
          }
        }
      }
    }
  }

}
