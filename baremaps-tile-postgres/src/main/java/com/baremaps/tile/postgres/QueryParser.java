package com.baremaps.tile.postgres;

import com.baremaps.config.tileset.Layer;
import com.baremaps.config.tileset.Query;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;

class QueryParser {

  private QueryParser() {

  }

  public static QueryValue parseQuery(Layer layer, Query query) {
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
        public void visit(SelectExpressionItem selectExpressionItem) {
          selectExpressionItem.setAlias(null);
        }
      });
    }

    return new QueryValue(layer, query, plainSelect);
  }
}
