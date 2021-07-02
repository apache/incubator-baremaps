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

import com.baremaps.config.tileset.Layer;
import com.baremaps.config.tileset.Query;
import java.util.Objects;
import java.util.Optional;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;

public class PostgresQueryParser {

  private PostgresQueryParser() {

  }

  public static Parse parse(Layer layer, Query query) {
    // Try to parse the query
    PlainSelect plainSelect;
    try {
      Select select = (Select) CCJSqlParserUtil.parse(query.getSql());
      plainSelect = (PlainSelect) select.getSelectBody();
    } catch (JSQLParserException e) {
      String message = String.format("The layer '%s' contains a malformed query.\n"
          + "\tActual query:\n\t\t%s", layer.getId(), query.getSql());
      throw new IllegalArgumentException(message, e);
    }

    // Check the number of columns
    if (plainSelect.getSelectItems().size() != 3) {
      String message = String.format("The layer '%s' contains a malformed query.\n"
          + "\tExpected format:\n\t\tSELECT c1::bigint, c2::hstore, c3::geometry FROM t WHERE c\n"
          + "\tActual query:\n\t\t%s", layer.getId(), query.getSql());
      throw new IllegalArgumentException(message);
    }

    // Remove aliases
    for (SelectItem item : plainSelect.getSelectItems()) {
      item.accept(new SelectItemVisitorAdapter() {
        public void visit(SelectExpressionItem item) {
          item.setAlias(null);
        }
      });
    }

    return new Parse(layer, query, plainSelect);
  }

  public static class Parse {

    private final Layer layer;
    private final Query query;
    private final PlainSelect parse;

    private Parse(
        Layer layer,
        Query query,
        PlainSelect result) {
      this.layer = layer;
      this.query = query;
      this.parse = result;
    }

    public Layer getLayer() {
      return layer;
    }

    public Query getQuery() {
      return query;
    }

    public PlainSelect getParse() {
      return parse;
    }

    public String getSource() {
      return "H" + Math.abs(Objects.hash(
          PlainSelect.getStringList(parse.getSelectItems()),
          parse.getFromItem().toString(),
          PlainSelect.getStringList(parse.getJoins())
      ));
    }

    public String getId() {
      return parse.getSelectItems().get(0).toString();
    }

    public String getTags() {
      return parse.getSelectItems().get(1).toString();
    }

    public String getGeom() {
      return parse.getSelectItems().get(2).toString();
    }

    public String getFrom() {
      StringBuilder sql = new StringBuilder();
      sql.append(parse.getFromItem());
      if (parse.getJoins() != null) {
        for (Join join : parse.getJoins()) {
          if (join.isSimple()) {
            sql.append(", ").append(join);
          } else {
            sql.append(" ").append(join);
          }
        }
      }
      return sql.toString();
    }

    public Optional<String> getWhere() {
      return Optional.ofNullable(parse.getWhere()).map(where -> where.toString());
    }
  }

}
