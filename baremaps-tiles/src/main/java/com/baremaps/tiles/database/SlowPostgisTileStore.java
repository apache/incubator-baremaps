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

package com.baremaps.tiles.database;

import com.baremaps.tiles.config.Config;
import com.baremaps.tiles.config.Layer;
import com.baremaps.util.tile.Tile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SlowPostgisTileStore extends PostgisTileStore {

  private static Logger logger = LogManager.getLogger();

  private static final String SQL_LAYER = "SELECT ST_AsMVT(mvt_geom, ''{0}'', 4096, ''geom'') FROM {1}";

  private static final String SQL_QUERY = "SELECT {0} AS id, {1} AS tags, {2} AS geom FROM {3}{4}";

  private static final String SQL_WHERE = " WHERE {0}";

  private static final String SQL_SOURCE =
      "(SELECT id, "
          + "hstore_to_jsonb_loose(tags || hstore(''geometry'', "
          + "(lower(replace(st_geometrytype(geom), ''ST_'', ''''))))), "
          + "ST_AsMvtGeom(geom, {2}, 4096, 256, true) AS geom "
          + "FROM ({1}) AS layer "
          + "WHERE ST_Intersects(geom, {2})"
          + ") as mvt_geom";

  private static final CharSequence SQL_UNION_ALL = " UNION ALL ";

  private final PoolingDataSource datasource;

  private final Config config;

  public SlowPostgisTileStore(PoolingDataSource datasource, Config config) {
    this.datasource = datasource;
    this.config = config;
  }

  @Override
  public byte[] read(Tile tile) throws IOException {
    try (Connection connection = datasource.getConnection();
        ByteArrayOutputStream data = new ByteArrayOutputStream()) {

      int length = 0;
      GZIPOutputStream gzip = new GZIPOutputStream(data);
      for (Layer layer : config.getLayers()) {
        if (layer.getMinZoom() <= tile.getZ() && tile.getZ() < layer.getMaxZoom()) {
          String sql = query(tile, layer);
          logger.debug("Executing tile query: {}", sql);
          try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
              byte[] bytes = resultSet.getBytes(1);
              length += bytes.length;
              gzip.write(bytes);
            }
          }
        }
      }
      gzip.close();

      if (length > 0) {
        return data.toByteArray();
      } else {
        return null;
      }
    } catch (IOException | SQLException ex) {
      throw new IOException(ex);
    }
  }

  private String query(Tile tile, Layer layer) {
    return MessageFormat.format(SQL_LAYER,
        layer.getId(),
        MessageFormat.format(SQL_SOURCE,
            layer.getId(),
            layer.getQueries().stream()
                .map(query -> QueryParser.parse(layer, query))
                .map(query -> MessageFormat.format(SQL_QUERY,
                    query.getId(),
                    query.getTags(),
                    query.getGeom(),
                    query.getFrom(),
                    query.getWhere().map(where -> MessageFormat.format(SQL_WHERE, where))
                        .orElse("")))
                .collect(Collectors.joining(SQL_UNION_ALL)),
            envelope(tile)));
  }

  @Override
  public void write(Tile tile, byte[] bytes) throws IOException {
    throw new UnsupportedOperationException("The tile store is readonly");
  }

  @Override
  public void delete(Tile tile) throws IOException {
    throw new UnsupportedOperationException("The tile store is readonly");
  }

}
