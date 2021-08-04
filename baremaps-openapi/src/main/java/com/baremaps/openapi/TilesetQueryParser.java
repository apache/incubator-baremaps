package com.baremaps.openapi;

import com.baremaps.model.Layer;
import com.baremaps.model.Query;
import com.baremaps.model.TileSet;
import com.baremaps.tile.Tile;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;

public class TilesetQueryParser {

  private static final String TILE_ENVELOPE = "st_tileenvelope(%1$s, %2$s, %3$s)";

  private static final String WITH_QUERY = "with %1$s %2$s";

  private static final String SOURCE_QUERY = ""
      + "%1$s as ("
      + "select "
      + "id, "
      + "(tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, "
      + "st_asmvtgeom(geom, $envelope, 4096, 256, true) as geom "
      + "from ("
      + "select %2$s as id, %3$s as tags, %4$s as geom from %5$s%6$s"
      + ") as source "
      + "where %7$s st_intersects(geom, $envelope)"
      + ")";

  private static final String SOURCE_WHERE = "(%s) and";

  private static final String TARGET_QUERY = ""
      + "select "
      + "st_asmvt(target, '%1$s', 4096, 'geom', 'id') "
      + "from (%2$s) as target";

  private static final String TARGET_LAYER_QUERY = ""
      + "select id, hstore_to_jsonb_loose(tags) as tags, geom from %1$s %2$s";

  private static final String TARGET_WHERE = "where %s";

  private static final String UNION = " union all ";

  private static final String COMMA = ", ";

  private static final String SPACE = " ";

  private static final String EMPTY = "";

  public TilesetQueryParser() {}

  public String parse(TileSet tileset, Tile tile) {
    List<ParsedQuery> queries = tileset.getLayers().stream()
        .flatMap(layer -> layer.getQueries().stream().map(query -> parseQuery(layer, query)))
        .collect(Collectors.toList());
    String sourceQueries = sourceQueries(queries, tile);
    String targetQueries = targetQueries(queries, tile);
    String withQuery = String.format(WITH_QUERY, sourceQueries, targetQueries);
    Map<String, String> variables = Map.of(
        "envelope", tileEnvelope(tile),
        "zoom", String.valueOf(tile.z()));
    return interpolate(variables, withQuery);
  }

  private ParsedQuery parseQuery(Layer layer, Query query) {
    // Try to parse the query
    PlainSelect plainSelect;
    try {
      Select select = (Select) CCJSqlParserUtil.parse(query.getSql());
      plainSelect = (PlainSelect) select.getSelectBody();
    } catch (JSQLParserException e) {
      String message = String.format("The layer '%s' contains a malformed query.\n"
          + "\tQuery:\n\t\t%s", layer.getId(), query.getSql());
      throw new IllegalArgumentException(message, e);
    }

    // Check the number of columns
    if (plainSelect.getSelectItems().size() != 3) {
      String message = String.format("The layer '%s' contains a malformed query.\n"
          + "\tExpected format:\n\t\tSELECT c1::bigint, c2::hstore, c3::geometry FROM t WHERE c\n"
          + "\tActual query:\n\t\t%s", layer.getId(), query.getSql());
      throw new IllegalArgumentException(message);
    }

    // Remove all the aliases
    for (SelectItem selectItem : plainSelect.getSelectItems()) {
      selectItem.accept(new SelectItemVisitorAdapter() {
        @Override
        public void visit(SelectExpressionItem selectExpressionItem) {
          selectExpressionItem.setAlias(null);
        }
      });
    }
    return new ParsedQuery(layer, query, plainSelect);
  }

  private String sourceQueries(List<ParsedQuery> queries, Tile tile) {
    return queries.stream()
        .filter(query -> zoomFilter(tile, query.getQuery()))
        .collect(
            Collectors.groupingBy(this::commonTableExpression, LinkedHashMap::new, Collectors.toList()))
        .entrySet().stream()
        .map(entry -> sourceQuery(entry.getKey(), entry.getValue()))
        .distinct()
        .collect(Collectors.joining(COMMA));
  }

  private String sourceQuery(CommonTableExpression queryKey, List<ParsedQuery> queryValues) {
    String alias = queryKey.getAlias();
    String id = queryKey.getSelectItems().get(0).toString();
    String tags = queryKey.getSelectItems().get(1).toString();
    String geom = queryKey.getSelectItems().get(2).toString();
    String from = queryKey.getFromItem().toString();
    String joins = Optional.ofNullable(queryKey.getJoins())
        .stream().flatMap(List::stream)
        .map(Join::toString)
        .collect(Collectors.joining(SPACE));
    String where = queryValues.stream()
        .map(query -> query.getValue().getWhere())
        .map(Optional::ofNullable)
        .flatMap(Optional::stream)
        .map(Parenthesis::new)
        .map(Expression.class::cast)
        .reduce(OrExpression::new)
        .map(expression -> String.format(SOURCE_WHERE, expression))
        .orElse(EMPTY);
    return String.format(SOURCE_QUERY, alias, id, tags, geom, from, joins, where);
  }

  private String targetQueries(List<ParsedQuery> queries, Tile tile) {
    return queries.stream()
        .filter(query -> zoomFilter(tile, query.getQuery()))
        .collect(Collectors.groupingBy(ParsedQuery::getLayer, LinkedHashMap::new, Collectors.toList()))
        .entrySet().stream()
        .map(entry -> targetQuery(entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(UNION));
  }

  private String targetQuery(Layer layer, List<ParsedQuery> queryValues) {
    return String.format(TARGET_QUERY, layer.getId(), queryValues.stream()
        .map(this::targetLayerQuery)
        .collect(Collectors.joining(UNION)));
  }

  private String targetLayerQuery(ParsedQuery queryValue) {
    String alias = commonTableExpression(queryValue).getAlias();
    String where = Optional.ofNullable(queryValue.getValue().getWhere())
        .map(expression -> String.format(TARGET_WHERE, expression))
        .orElse(EMPTY);
    return String.format(TARGET_LAYER_QUERY, alias, where);
  }

  private boolean zoomFilter(Tile tile, Query query) {
    return query.getMinzoom() <= tile.z() && tile.z() < query.getMaxzoom();
  }

  private CommonTableExpression commonTableExpression(ParsedQuery query) {
    return new CommonTableExpression(
        query.getValue().getSelectItems(),
        query.getValue().getFromItem(),
        query.getValue().getJoins());
  }

  private String tileEnvelope(Tile tile) {
    return String.format(TILE_ENVELOPE, tile.z(), tile.x(), tile.y());
  }

  private String interpolate(Map<String, String> variables, String string) {
    for (Entry<String, String> entry : variables.entrySet()) {
      string = string.replace(String.format("$%s", entry.getKey()), entry.getValue());
    }
    return string;
  }


  private class ParsedQuery {

    private final Layer layer;
    private final Query query;
    private final PlainSelect value;

    private ParsedQuery(Layer layer, Query query, PlainSelect parse) {
      this.layer = layer;
      this.query = query;
      this.value = parse;
    }

    private Layer getLayer() {
      return layer;
    }

    private Query getQuery() {
      return query;
    }

    private PlainSelect getValue() {
      return value;
    }

  }


  private class CommonTableExpression {

    private final List<SelectItem> selectItems;
    private final FromItem fromItem;
    private final List<Join> joins;

    private CommonTableExpression(
        List<SelectItem> selectItems,
        FromItem fromItem,
        List<Join> joins) {
      this.selectItems = selectItems;
      this.fromItem = fromItem;
      this.joins = joins;
    }

    private List<SelectItem> getSelectItems() {
      return selectItems;
    }

    private FromItem getFromItem() {
      return fromItem;
    }

    private List<Join> getJoins() {
      return joins;
    }

    private String getAlias() {
      return String.format("h%x", hashCode()).substring(0, 9);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof CommonTableExpression)) {
        return false;
      }
      return hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
      String selectItemsString = selectItems.toString();
      String fromItemString = fromItem.toString();
      String joinsString = Optional.ofNullable(joins).stream()
          .flatMap(List::stream)
          .map(Join::toString)
          .collect(Collectors.joining());
      return Objects.hash(selectItemsString, fromItemString, joinsString);
    }

  }

}