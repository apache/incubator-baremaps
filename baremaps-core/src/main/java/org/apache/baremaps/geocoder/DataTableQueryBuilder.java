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

package org.apache.baremaps.geocoder;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.baremaps.calcite.DataColumn;
import org.apache.baremaps.calcite.DataSchema;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * A builder for creating queries on a data table.
 */
public class DataTableQueryBuilder {

  private final Analyzer analyzer;

  private Map<String, Float> fields = new HashMap<>();

  private String query;

  /**
   * Constructs a query builder with the default analyzer.
   */
  public DataTableQueryBuilder() {
    this(GeocoderConstants.ANALYZER);
  }

  /**
   * Constructs a query builder with the specified analyzer.
   *
   * @param analyzer the analyzer
   */
  public DataTableQueryBuilder(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  /**
   * Replace all the fields with the columns of the schema and a boost of 1.0.
   *
   * @param schema the schema
   * @return the query builder
   */
  public DataTableQueryBuilder schema(DataSchema schema) {
    this.fields = new HashMap<>(schema.columns().stream()
        .collect(Collectors.toMap(DataColumn::name, column -> 1.0f)));
    return this;
  }

  /**
   * Replace all the fields with the specified fields and boosts.
   *
   * @param fields the fields and boosts
   * @return the query builder
   */
  public DataTableQueryBuilder columns(Map<DataColumn, Float> fields) {
    this.fields = new HashMap<>(fields.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue)));
    return this;
  }

  /**
   * Add a column with a specified boost.
   *
   * @param column the column
   * @param boost the boost
   * @return the query builder
   */
  public DataTableQueryBuilder column(DataColumn column, float boost) {
    return column(column.name(), boost);
  }

  /**
   * Add a column with a specified boost.
   *
   * @param column the column
   * @param boost the boost
   * @return the query builder
   */
  public DataTableQueryBuilder column(String column, float boost) {
    fields.put(column, boost);
    return this;
  }

  /**
   * Set the query text.
   *
   * @param query the query text
   * @return the query builder
   */
  public DataTableQueryBuilder query(String query) {
    this.query = query;
    return this;
  }

  /**
   * Build the query.
   *
   * @return the query
   */
  public Query build() {
    var builder = new BooleanQuery.Builder();

    var parser = new SimpleQueryParser(analyzer, fields);
    var escapedQuery = QueryParserBase.escape(query);
    var termsQuery = parser.parse(escapedQuery);

    // at least one terms of the queryText must be present
    builder.add(termsQuery, BooleanClause.Occur.MUST);
    return builder.build();
  }
}
