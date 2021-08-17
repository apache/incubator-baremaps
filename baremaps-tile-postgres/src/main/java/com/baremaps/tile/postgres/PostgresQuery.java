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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;

public class PostgresQuery {

  private final String layer;
  private final Integer minzoom;
  private final Integer maxzoom;
  private final String sql;
  private final PlainSelect ast;

  public PostgresQuery(String layer, Integer minzoom, Integer maxzoom, String sql) {
    this.layer = layer;
    this.minzoom = minzoom;
    this.maxzoom = maxzoom;
    this.sql = sql;
    this.ast = parse(sql);
  }

  public String getLayer() {
    return layer;
  }

  public Integer getMinzoom() {
    return minzoom;
  }

  public Integer getMaxzoom() {
    return maxzoom;
  }

  public String getSql() {
    return sql;
  }

  public PlainSelect getAst() {
    return ast;
  }

  private PlainSelect parse(String query) {
    // Try to parse the query
    PlainSelect plainSelect;
    try {
      Select select = (Select) CCJSqlParserUtil.parse(query);
      plainSelect = (PlainSelect) select.getSelectBody();
    } catch (JSQLParserException e) {
      String message = String.format("The query is malformed.\n" + "\tQuery:\n\t\t%s", query);
      throw new IllegalArgumentException(message, e);
    }

    // Check the number of columns
    if (plainSelect.getSelectItems().size() != 3) {
      String message =
          String.format(
              "The query is malformed.\n"
                  + "\tExpected format:\n\t\tSELECT c1::bigint, c2::hstore, c3::geometry FROM t WHERE c\n"
                  + "\tActual query:\n\t\t%s",
              query);
      throw new IllegalArgumentException(message);
    }

    // Remove all the aliases
    for (SelectItem selectItem : plainSelect.getSelectItems()) {
      selectItem.accept(
          new SelectItemVisitorAdapter() {
            @Override
            public void visit(SelectExpressionItem selectExpressionItem) {
              selectExpressionItem.setAlias(null);
            }
          });
    }

    return plainSelect;
  }
}
