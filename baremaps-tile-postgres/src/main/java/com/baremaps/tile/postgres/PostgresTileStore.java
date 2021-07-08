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

package com.baremaps.tile.postgres;

import static com.baremaps.config.VariableUtils.interpolate;
import static com.baremaps.tile.postgres.QueryParser.parseQuery;

import com.baremaps.config.tileset.Query;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.tile.Tile;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.TileStoreException;
import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import javax.sql.DataSource;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.statement.select.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresTileStore implements TileStore {

  private static final Logger logger = LoggerFactory.getLogger(PostgresTileStore.class);

  private static final String TILE_ENVELOPE = "st_tileenvelope(%1$s, %2$s, %3$s)";

  private static final String WITH_QUERY = "WITH %1$s %2$s";

  private static final String SOURCE_QUERY = ""
      + "%1$s as ("
      + "select "
      + "id, "
      + "(tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, "
      + "st_asmvtgeom(geom, $envelope, 4096, 256, true) as geom "
      + "from ("
      + "select %2$s as id, %3$s as tags, %4$s as geom from %5$s%6$s"
      + ") as source "
      + "where (%7$s) and st_intersects(geom, $envelope)"
      + ")";

  private static final String TARGET_QUERY = ""
      + "select "
      + "st_asmvt(target, '%1$s', 4096) "
      + "from ("
      + "select id, hstore_to_jsonb_loose(tags), geom from %2$s where %3$s"
      + ") as target";

  private final DataSource datasource;

  private final List<QueryValue> queries;

  public PostgresTileStore(DataSource datasource, Tileset tileset) {
    this.datasource = datasource;
    this.queries = tileset.getLayers().stream()
        .flatMap(layer -> layer.getQueries().stream().map(query -> parseQuery(layer, query)))
        .collect(Collectors.toList());
  }

  public byte[] read(Tile tile) throws TileStoreException {
    try (Connection connection = datasource.getConnection();
        Statement statement = connection.createStatement();
        ByteArrayOutputStream data = new ByteArrayOutputStream()) {

      String sql = withQuery(tile);

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
    } catch (SQLException | IOException e) {
      throw new TileStoreException(e);
    }
  }

  protected String withQuery(Tile tile) {
    Map<QueryKey, List<QueryValue>> querySelection = selectQueries(queries, tile);
    String sourceQueries = sourceQueries(querySelection);
    String targetQueries = targetQueries(querySelection);
    String withQuery = String.format(WITH_QUERY, sourceQueries, targetQueries);
    Map<String, String> variables = ImmutableMap.of(
        "envelope", envelope(tile),
        "zoom", String.valueOf(tile.z()));
    return interpolate(variables, withQuery);
  }

  protected Map<QueryKey, List<QueryValue>> selectQueries(List<QueryValue> queries, Tile tile) {
    return queries.stream()
        .filter(query -> zoomFilter(tile, query.getQuery()))
        .collect(Collectors.groupingBy(query -> new QueryKey(
            query.getValue().getSelectItems(),
            query.getValue().getFromItem(),
            query.getValue().getJoins())));
  }

  protected String sourceQueries(Map<QueryKey, List<QueryValue>> querySelection) {
    return querySelection.entrySet().stream()
        .map(entry -> sourceQuery(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(", "));
  }

  protected String sourceQuery(QueryKey queryKey, List<QueryValue> queryValues) {
    String alias = queryKey.getAlias();
    String id = queryKey.getSelectItems().get(0).toString();
    String tags = queryKey.getSelectItems().get(1).toString();
    String geom = queryKey.getSelectItems().get(2).toString();
    String from = queryKey.getFromItem().toString();
    String joins = Optional.ofNullable(queryKey.getJoins())
        .stream().flatMap(List::stream)
        .map(Join::toString)
        .collect(Collectors.joining(" "));
    String where = queryValues.stream()
        .map(query -> query.getValue().getWhere())
        .map(Parenthesis::new)
        .map(Expression.class::cast)
        .reduce(OrExpression::new)
        .map(Expression::toString)
        .orElse("");
    return String.format(SOURCE_QUERY, alias, id, tags, geom, from, joins, where);
  }

  protected String targetQueries(Map<QueryKey, List<QueryValue>> querySelection) {
    return querySelection.entrySet().stream()
        .flatMap(group -> group.getValue().stream().map(entry -> Map.entry(group.getKey(), entry)))
        .map(entry -> targetQuery(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(" union all "));
  }

  protected String targetQuery(QueryKey group, QueryValue query) {
    String layer = query.getLayer().getId();
    String alias = group.getAlias();
    String where = query.getValue().getWhere().toString();
    return String.format(TARGET_QUERY, layer, alias, where);
  }

  protected boolean zoomFilter(Tile tile, Query query) {
    return query.getMinZoom() <= tile.z() && tile.z() < query.getMaxZoom();
  }

  protected String envelope(Tile tile) {
    return String.format(TILE_ENVELOPE, tile.z(), tile.x(), tile.y());
  }

  public void write(Tile tile, byte[] bytes) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }

  public void delete(Tile tile) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }

}