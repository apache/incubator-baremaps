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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.config.tileset.Query;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PostgisQueryParserTest {

  @Test
  void parse1() {
    parse(
        new Query(0, 1, "SELECT id, tags, geom FROM table"),
        "id",
        "tags",
        "geom",
        "table",
        Optional.empty());
  }

  @Test
  void parse2() {
    parse(
        new Query(0, 1, "select id, tags, geom from table"),
        "id",
        "tags",
        "geom",
        "table",
        Optional.empty());
  }

  @Test
  void parse3() {
    parse(
        new Query(0, 1, "SELECT id AS a, tags AS b, geom AS c FROM table"),
        "id",
        "tags",
        "geom",
        "table",
        Optional.empty());
  }

  @Test
  void parse4() {
    parse(
        new Query(0, 1, "select id as a, tags as b, geom as c from table"),
        "id",
        "tags",
        "geom",
        "table",
        Optional.empty());
  }

  @Test
  void parse5() {
    parse(
        new Query(0, 1, "SELECT id, tags, geom FROM table WHERE condition"),
        "id",
        "tags",
        "geom",
        "table",
        Optional.of("condition"));
  }

  @Test
  void parse6() {
    parse(
        new Query(
            0,
            1,
            "SELECT id, tags, geom FROM table WHERE tags ? 'building' AND st_geometrytype(geom) LIKE 'ST_Polygon'"),
        "id",
        "tags",
        "geom",
        "table",
        Optional.of("tags ? 'building' AND st_geometrytype(geom) LIKE 'ST_Polygon'"));
  }

  @Test
  void parse7() {
    parse(
        new Query(0, 1, "select id, tags, geom from table where condition"),
        "id",
        "tags",
        "geom",
        "table",
        Optional.of("condition"));
  }

  @Test
  void parse8() {
    parse(
        new Query(
            0, 1, "SELECT id, hstore(ARRAY['tag1', 'tag2'], ARRAY[tag1, tag2]), geom FROM table"),
        "id",
        "hstore(ARRAY['tag1', 'tag2'], ARRAY[tag1, tag2])",
        "geom",
        "table",
        Optional.empty());
  }

  @Test
  void parse9() {
    parse(
        new Query(0, 1, "SELECT id, hstore('tag', tag), geom FROM table"),
        "id",
        "hstore('tag', tag)",
        "geom",
        "table",
        Optional.empty());
  }

  @Test
  void parse10() {
    parse(
        new Query(0, 1, "SELECT id, hstore('tag', tag) as tags, geom FROM table"),
        "id",
        "hstore('tag', tag)",
        "geom",
        "table",
        Optional.empty());
  }

  @Test
  void parse11() {
    parse(
        new Query(0, 1, "SELECT id, tags, st_transform(geom, '1234') as geom FROM table"),
        "id",
        "tags",
        "st_transform(geom, '1234')",
        "table",
        Optional.empty());
  }

  @Test
  void parse12() {
    parse(
        new Query(0, 1, "SELECT id, a(b(c), d(e)), geom FROM table"),
        "id",
        "a(b(c), d(e))",
        "geom",
        "table",
        Optional.empty());
  }

  void parse(
      Query query, String id, String tags, String geom, String from, Optional<String> where) {
    PostgresQuery q1 =
        new PostgresQuery("layer", query.getMinZoom(), query.getMaxZoom(), query.getSql());
    assertEquals(id, String.valueOf(q1.getAst().getSelectItems().get(0)));
    assertEquals(tags, String.valueOf(q1.getAst().getSelectItems().get(1)));
    assertEquals(geom, String.valueOf(q1.getAst().getSelectItems().get(2)));
    assertEquals(from, String.valueOf(q1.getAst().getFromItem()));
    assertEquals(where, Optional.ofNullable(q1.getAst().getWhere()).map(String::valueOf));
  }
}
