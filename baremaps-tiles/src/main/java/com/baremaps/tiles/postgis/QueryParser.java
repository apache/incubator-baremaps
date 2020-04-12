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

package com.baremaps.tiles.postgis;

import com.baremaps.tiles.config.Layer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {

  private static final Pattern QUERY_PATTERN = Pattern
      .compile("SELECT\\s(.*?)\\sFROM\\s(.*?)(?:\\sWHERE\\s(.*))?", Pattern.CASE_INSENSITIVE);

  private static final Pattern COLUMN_PATTERN = Pattern
      .compile("(.*?)(?:\\sAS\\s(.*))?", Pattern.CASE_INSENSITIVE);

  private QueryParser() {

  }

  public static Query parse(Layer layer, String sql) {
    Matcher matcher = QUERY_PATTERN.matcher(sql);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("The SQL query malformed");
    }

    String select = matcher.group(1).trim();
    String from = matcher.group(2).trim();
    Optional<String> where = Optional.ofNullable(matcher.group(3)).map(s -> s.trim());

    List<String> columns = split(select);
    if (columns.size() != 3) {
      throw new IllegalArgumentException("The SQL query malformed");
    }

    String id = column(columns.get(0));
    String tags = column(columns.get(1));
    String geom = column(columns.get(2));

    String source = "H" + Math.abs(Objects.hash(id, tags, geom, from));

    return new Query(layer, source, id, tags, geom, from, where);
  }

  private static List<String> split(String s) {
    List<String> results = new ArrayList<String>();
    int level = 0;
    StringBuilder result = new StringBuilder();
    for (char c : s.toCharArray()) {
      if (c == ',' && level == 0) {
        results.add(result.toString());
        result.setLength(0);
      } else {
        if (c == '(' || c == '[') {
          level++;
        }
        if (c == ')' || c == ']') {
          level--;
        }
        result.append(c);
      }
    }
    results.add(result.toString());
    return results;
  }

  private static String column(String column) {
    Matcher matcher = COLUMN_PATTERN.matcher(column);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("The SQL query malformed");
    }
    return matcher.group(1).trim();
  }


  public static class Query {

    private final Layer layer;
    private final String source;
    private final String id;
    private final String tags;
    private final String geom;
    private final String from;
    private final Optional<String> where;

    private Query(Layer layer, String source, String id, String tags, String geom, String from,
        Optional<String> where) {
      this.layer = layer;
      this.source = source;
      this.id = id;
      this.tags = tags;
      this.geom = geom;
      this.from = from;
      this.where = where;
    }

    public Layer getLayer() {
      return layer;
    }

    public String getSource() {
      return source;
    }

    public String getId() {
      return id;
    }

    public String getTags() {
      return tags;
    }

    public String getGeom() {
      return geom;
    }

    public String getFrom() {
      return from;
    }

    public Optional<String> getWhere() {
      return where;
    }

  }

}
