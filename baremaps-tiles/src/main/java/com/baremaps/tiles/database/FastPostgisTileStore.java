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
import com.baremaps.tiles.database.QueryParser.Query;
import com.baremaps.util.tile.Tile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FastPostgisTileStore extends PostgisTileStore {

  private static Logger logger = LogManager.getLogger();

  private static final String WITH = "WITH {0} {1}";

  private static final String SOURCE = "{0} AS (SELECT id, "
      + "(tags || hstore(''geometry'', lower(replace(st_geometrytype(geom), ''ST_'', '''')))) as tags, "
      + "ST_AsMvtGeom(geom, {5}, 4096, 256, true) AS geom "
      + "FROM (SELECT {1} as id, {2} as tags, {3} as geom FROM {4}) AS {0} WHERE ST_Intersects(geom, {5}))";

  private static final String LAYER = "SELECT ST_AsMVT(mvt_geom, ''{0}'', 4096) FROM ({1}) as mvt_geom";

  private static final String QUERY = "SELECT id, hstore_to_jsonb_loose(tags), geom FROM {3}";

  private static final String WHERE = " WHERE {0}";

  private static final String COMMA = ", ";

  private static final String UNION_ALL = " UNION All ";

  private final PoolingDataSource datasource;

  private final Config config;

  private final Map<Layer, List<Query>> queries;

  public FastPostgisTileStore(PoolingDataSource datasource, Config config) {
    this.datasource = datasource;
    this.config = config;
    this.queries = config.getLayers().stream()
        .flatMap(layer -> layer.getQueries().stream().map(query -> QueryParser.parse(layer, query)))
        .collect(Collectors.groupingBy(q -> q.getLayer()));
  }

  @Override
  public byte[] read(Tile tile) throws IOException {
    try (Connection connection = datasource.getConnection()) {
      try (Statement statement = connection.createStatement();
          ByteArrayOutputStream data = new ByteArrayOutputStream()) {

        String sql = query(tile);
        logger.debug("Executing tile query: {}", sql);
        ResultSet resultSet = statement.executeQuery(sql);

        int length = 0;
        GZIPOutputStream gzip = new GZIPOutputStream(data);
        while (resultSet.next()) {
          byte[] bytes = resultSet.getBytes(1);
          length += bytes.length;
          gzip.write(bytes);
        }
        gzip.close();

        if (length > 0) {
          return data.toByteArray();
        } else {
          return null;
        }
      }
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  private String query(Tile tile) {
    String sources = queries.entrySet().stream()
        .filter(
            entry -> entry.getKey().getMinZoom() <= tile.getZ() && entry.getKey().getMaxZoom() >= tile.getZ())
        .flatMap(entry -> entry.getValue().stream().map(query -> MessageFormat.format(SOURCE,
            query.getSource(),
            query.getId(),
            query.getTags(),
            query.getGeom(),
            query.getFrom(),
            envelope(tile))))
        .collect(Collectors.toSet())
        .stream()
        .collect(Collectors.joining(COMMA));
    String targets = queries.entrySet().stream()
        .filter(entry ->
            entry.getKey().getMinZoom() <= tile.getZ() && entry.getKey().getMaxZoom() >= tile.getZ())
        .map(entry -> {
          String queries = entry.getValue().stream()
              .map(select -> {
                String l = MessageFormat.format(QUERY,
                    select.getId(),
                    select.getTags(),
                    select.getGeom(),
                    select.getSource());
                String r = select.getWhere()
                    .map(s -> MessageFormat.format(WHERE, s))
                    .orElse("");
                return l + r;
              })
              .collect(Collectors.joining(UNION_ALL));
          return MessageFormat.format(LAYER, entry.getKey().getName(), queries);
        })
        .collect(Collectors.joining(UNION_ALL));
    return MessageFormat.format(WITH, sources, targets);
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