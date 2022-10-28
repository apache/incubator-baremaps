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

package org.apache.baremaps.database.tile;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
import org.apache.baremaps.tileset.Tileset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read-only {@code TileStore} implementation that uses the PostgreSQL to generate vector tiles.
 * This {@code TileStore} combines the input queries, identifies common table expressions (CTE), and
 * generates a single optimized query that hits the database.
 */
public class PostgresTileStore implements TileStore {

  private static final Logger logger = LoggerFactory.getLogger(PostgresTileStore.class);

  private static final String TILE_ENVELOPE = "st_tileenvelope(%1$s, %2$s, %3$s)";

  private static final String WITH_QUERY = "with %1$s %2$s";

  private static final String CTE_QUERY =
      "%1$s as (select * from %3$s%4$s where %5$s st_intersects(%2$s, $envelope))";

  private static final String CTE_WHERE = "(%s) and";

  private static final String STATEMENT_QUERY =
      "select st_asmvt(target, '%1$s', 4096, 'geom', 'id') from (%2$s) as target";

  private static final String STATEMENT_LAYER_QUERY = "select " + "%1$s as id, "
      + "(%2$s ||  jsonb_build_object('geometry', lower(replace(st_geometrytype(%3$s), 'ST_', '')))) as tags, "
      + "st_asmvtgeom(%3$s, $envelope, 4096, 256, true) as geom " + "from %4$s %5$s";

  private static final String STATEMENT_WHERE = "where %s";

  private static final String UNION = " union all ";

  private static final String COMMA = ", ";

  private static final String SPACE = " ";

  private static final String EMPTY = "";

  public static final String CONTENT_ENCODING = "gzip";

  public static final String CONTENT_TYPE = "application/vnd.mapbox-vector-tile";

  private final DataSource datasource;

  private final List<PostgresQuery> queries;

  public PostgresTileStore(DataSource datasource, List<PostgresQuery> queries) {
    this.datasource = datasource;
    this.queries = queries;
  }

  public PostgresTileStore(DataSource datasource, Tileset tileset) {
    this.datasource = datasource;
    this.queries = tileset.getVectorLayers().stream()
        .flatMap(layer -> layer.getQueries().stream().map(query -> new PostgresQuery(layer.getId(),
            query.getMinzoom(), query.getMaxzoom(), query.getSql())))
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  public ByteBuffer read(Tile tile) throws TileStoreException {
    try (Connection connection = datasource.getConnection();
        Statement statement = connection.createStatement();
        ByteArrayOutputStream data = new ByteArrayOutputStream()) {

      String sql = withQuery(tile);
      logger.debug("Executing query: {}", sql);

      int length = 0;
      try (GZIPOutputStream gzip = new GZIPOutputStream(data);
          ResultSet resultSet = statement.executeQuery(sql)) {
        while (resultSet.next()) {
          byte[] bytes = resultSet.getBytes(1);
          length += bytes.length;
          gzip.write(bytes);
        }
      }

      if (length > 0) {
        return ByteBuffer.wrap(data.toByteArray());
      } else {
        return null;
      }
    } catch (SQLException | IOException e) {
      throw new TileStoreException(e);
    }
  }

  /**
   * Returns a WITH query for the provided tile.
   *
   * @param tile the tile
   * @return the WITH query
   */
  protected String withQuery(Tile tile) {
    int zoom = tile.z();
    String sourceQueries = ctes(queries, zoom);
    String targetQueries = statements(queries, zoom);
    String withQuery = String.format(WITH_QUERY, sourceQueries, targetQueries);
    Map<String, String> variables =
        Map.of("envelope", tileEnvelope(tile), "zoom", String.valueOf(zoom));
    return VariableUtils.interpolate(variables, withQuery);
  }

  /**
   * Returns the common table expressions for a list of input queries at a specified zoom level.
   *
   * @param queries the queries
   * @param zoom the zoom level
   * @return the common table expressions
   */
  protected String ctes(List<PostgresQuery> queries, int zoom) {
    return queries.stream().filter(query -> zoomPredicate(query, zoom))
        .collect(Collectors.groupingBy(this::commonTableExpression, LinkedHashMap::new,
            Collectors.toList()))
        .entrySet().stream().map(entry -> cte(entry.getKey(), entry.getValue())).distinct()
        .collect(Collectors.joining(COMMA));
  }

  /**
   * Returns the common table expression for a group of queries.
   *
   * @param group the common table expression
   * @param queries the input queries associated with the provided group
   * @return the common table expression
   */
  protected String cte(PostgresGroup group, List<PostgresQuery> queries) {
    String alias = group.getAlias();
    String geom = group.getSelectItems().get(2).toString();
    String from = group.getFromItem().toString();
    String joins = Optional.ofNullable(group.getJoins()).stream().flatMap(List::stream)
        .map(Join::toString).collect(Collectors.joining(SPACE));
    String where = queries.stream().map(query -> query.getAst().getWhere())
        .map(Optional::ofNullable).map(o -> o.orElse(new Column("true"))).map(Parenthesis::new)
        .map(Expression.class::cast).reduce(OrExpression::new)
        .map(expression -> String.format(CTE_WHERE, expression)).orElse(EMPTY);
    return String.format(CTE_QUERY, alias, geom, from, joins, where);
  }

  /**
   * Returns the statements for a list of input queries at a specified zoom level.
   *
   * @param queries the queries
   * @param zoom the zoom level
   * @return the statements
   */
  protected String statements(List<PostgresQuery> queries, int zoom) {
    return queries.stream().filter(query -> zoomPredicate(query, zoom))
        .collect(
            Collectors.groupingBy(PostgresQuery::getLayer, LinkedHashMap::new, Collectors.toList()))
        .entrySet().stream().map(entry -> layerStatements(entry.getValue(), entry.getKey()))
        .collect(Collectors.joining(UNION));
  }

  /**
   * Returns the statements for a list of input queries corresponding to a layer.
   *
   * @param queries the queries
   * @param layer the layer name
   * @return the statements
   */
  protected String layerStatements(List<PostgresQuery> queries, String layer) {
    return String.format(STATEMENT_QUERY, layer, queries.stream()
        .map(queryValue -> layerStatement(queryValue)).collect(Collectors.joining(UNION)));
  }

  /**
   * Returns the statement for a query in a layer.
   *
   * @param query the query
   * @return the statement
   */
  protected String layerStatement(PostgresQuery query) {
    String alias = commonTableExpression(query).getAlias();
    var ast = query.getAst();
    String id = ast.getSelectItems().get(0).toString();
    String tags = ast.getSelectItems().get(1).toString();
    String geom = ast.getSelectItems().get(2).toString();
    String where = Optional.ofNullable(query.getAst().getWhere())
        .map(expression -> String.format(STATEMENT_WHERE, expression)).orElse(EMPTY);
    return String.format(STATEMENT_LAYER_QUERY, id, tags, geom, alias, where);
  }

  protected boolean zoomPredicate(PostgresQuery query, int zoom) {
    return query.getMinzoom() <= zoom && zoom < query.getMaxzoom();
  }

  protected PostgresGroup commonTableExpression(PostgresQuery query) {
    return new PostgresGroup(query.getAst().getSelectItems(), query.getAst().getFromItem(),
        query.getAst().getJoins());
  }

  protected String tileEnvelope(Tile tile) {
    return String.format(TILE_ENVELOPE, tile.z(), tile.x(), tile.y());
  }

  /** This operation is not supported. */
  public void write(Tile tile, ByteBuffer blob) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }

  /** This operation is not supported. */
  public void delete(Tile tile) {
    throw new UnsupportedOperationException("The postgis tile store is read only");
  }

}
