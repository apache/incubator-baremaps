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

import static com.baremaps.tile.VariableUtils.interpolate;

import com.baremaps.tile.Tile;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.TileStoreException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import javax.sql.DataSource;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresTileStore implements TileStore {

  private static final Logger logger = LoggerFactory.getLogger(PostgresTileStore.class);

  private static final String TILE_ENVELOPE = "st_tileenvelope(%1$s, %2$s, %3$s)";

  private static final String WITH_QUERY = "with %1$s %2$s";

  private static final String SOURCE_QUERY =
      "%1$s as (select * from %3$s%4$s where %5$s st_intersects(%2$s, $envelope))";

  private static final String SOURCE_WHERE = "(%s) and";

  private static final String TARGET_QUERY =
      "select st_asmvt(target, '%1$s', 4096, 'geom', 'id') from (%2$s) as target";

  private static final String TARGET_LAYER_QUERY =
      "select "
          + "%1$s as id, "
          + "(%2$s || ('{\"geometry\":\"' || lower(replace(st_geometrytype(geom), 'ST_', '')) ||'\"}')::jsonb) as tags, "
          + "st_asmvtgeom(%3$s, $envelope, 4096, 256, true) as geom "
          + "from %4$s %5$s";

  private static final String TARGET_WHERE = "where %s";

  private static final String UNION = " union all ";

  private static final String COMMA = ", ";

  private static final String SPACE = " ";

  private static final String EMPTY = "";

  private final DataSource datasource;

  private final List<PostgresQuery> queries;

  public PostgresTileStore(DataSource datasource, List<PostgresQuery> queries) {
    this.datasource = datasource;
    this.queries = queries;
  }

  public byte[] read(Tile tile) throws TileStoreException {
    try (Connection connection = datasource.getConnection();
        Statement statement = connection.createStatement();
        ByteArrayOutputStream data = new ByteArrayOutputStream()) {

      String sql = withQuery(tile);
      logger.debug("Executing query: {}", sql);
      logger.info("Executing query: {}", sql);

      int length = 0;
      GZIPOutputStream gzip = new GZIPOutputStream(data);
      ResultSet resultSet = statement.executeQuery(sql);
      while (resultSet.next()) {
        byte[] bytes = resultSet.getBytes(1);
        length += bytes.length;
        gzip.write(bytes);
      }
      gzip.close();
l
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
    String sourceQueries = sourceQueries(queries, tile);
    String targetQueries = targetQueries(queries, tile);
    String withQuery = String.format(WITH_QUERY, sourceQueries, targetQueries);
    Map<String, String> variables =
        Map.of(
            "envelope", tileEnvelope(tile),
            "zoom", String.valueOf(tile.z()));
    return interpolate(variables, withQuery);
  }

  protected String sourceQueries(List<PostgresQuery> queries, Tile tile) {
    return queries.stream()
        .filter(query -> zoomFilter(tile, query))
        .collect(
            Collectors.groupingBy(
                this::commonTableExpression, LinkedHashMap::new, Collectors.toList()))
        .entrySet()
        .stream()
        .map(entry -> sourceQuery(entry.getKey(), entry.getValue()))
        .distinct()
        .collect(Collectors.joining(COMMA));
  }

  protected String sourceQuery(PostgresCTE queryKey, List<PostgresQuery> queryValues) {
    String alias = queryKey.getAlias();
    String geom = queryKey.getSelectItems().get(2).toString();
    String from = queryKey.getFromItem().toString();
    String joins =
        Optional.ofNullable(queryKey.getJoins()).stream()
            .flatMap(List::stream)
            .map(Join::toString)
            .collect(Collectors.joining(SPACE));
    String where =
        queryValues.stream()
            .map(query -> query.getAst().getWhere())
            .map(Optional::ofNullable)
            .map(o -> o.orElse(new Column("true")))
            .map(Parenthesis::new)
            .map(Expression.class::cast)
            .reduce(OrExpression::new)
            .map(expression -> String.format(SOURCE_WHERE, expression))
            .orElse(EMPTY);
    return String.format(SOURCE_QUERY, alias, geom, from, joins, where);
  }

  protected String targetQueries(List<PostgresQuery> queries, Tile tile) {
    return queries.stream()
        .filter(query -> zoomFilter(tile, query))
        .collect(
            Collectors.groupingBy(PostgresQuery::getLayer, LinkedHashMap::new, Collectors.toList()))
        .entrySet()
        .stream()
        .map(entry -> targetQuery(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(UNION));
  }

  protected String targetQuery(String layer, List<PostgresQuery> queryValues) {
    return String.format(
        TARGET_QUERY,
        layer,
        queryValues.stream()
            .map(queryValue -> targetLayerQuery(queryValue))
            .collect(Collectors.joining(UNION)));
  }

  protected String targetLayerQuery(PostgresQuery queryValue) {
    String alias = commonTableExpression(queryValue).getAlias();
    var ast = queryValue.getAst();
    String id = ast.getSelectItems().get(0).toString();
    String tags = ast.getSelectItems().get(1).toString();
    String geom = ast.getSelectItems().get(2).toString();
    String where =
        Optional.ofNullable(queryValue.getAst().getWhere())
            .map(expression -> String.format(TARGET_WHERE, expression))
            .orElse(EMPTY);
    return String.format(TARGET_LAYER_QUERY, id, tags, geom, alias, where);
  }

  protected boolean zoomFilter(Tile tile, PostgresQuery query) {
    return query.getMinzoom() <= tile.z() && tile.z() < query.getMaxzoom();
  }

  public PostgresCTE commonTableExpression(PostgresQuery query) {
    return new PostgresCTE(
        query.getAst().getSelectItems(), query.getAst().getFromItem(), query.getAst().getJoins());
  }

  protected String tileEnvelope(Tile tile) {
    return String.format(TILE_ENVELOPE, tile.z(), tile.x(), tile.y());
  }

  public void write(Tile tile, byte[] bytes) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }

  public void delete(Tile tile) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }
}
