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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.tiles.config.Layer;
import com.baremaps.tiles.database.QueryParser;
import com.baremaps.tiles.database.QueryParser.Query;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class QueryParserTest {

  private class TestQuery {

    public final String query;
    public final String id;
    public final String tags;
    public final String geom;
    public final String from;
    public final Optional<String> where;

    public TestQuery(String query, String id, String tags, String geom, String from, Optional<String> where) {
      this.query = query;
      this.id = id;
      this.tags = tags;
      this.geom = geom;
      this.from = from;
      this.where = where;
    }

  }

  private final List<TestQuery> testQueries = ImmutableList.of(
      new TestQuery("SELECT id, tags, geom FROM table",
          "id", "tags", "geom", "table",
          Optional.empty()),
      new TestQuery("select id, tags, geom from table",
          "id", "tags", "geom", "table",
          Optional.empty()),
      new TestQuery("SELECT id AS a, tags AS b, geom AS c FROM table",
          "id", "tags", "geom", "table",
          Optional.empty()),
      new TestQuery("select id as a, tags as b, geom as c from table",
          "id", "tags", "geom", "table",
          Optional.empty()),
      new TestQuery("SELECT id, tags, geom FROM table WHERE condition",
          "id", "tags", "geom", "table",
          Optional.of("condition")),
      new TestQuery("select id, tags, geom from table where condition",
          "id", "tags", "geom", "table",
          Optional.of("condition")),
      new TestQuery("SELECT id, hstore('tag', tag), geom FROM table",
          "id", "hstore('tag', tag)", "geom", "table",
          Optional.empty()),
      new TestQuery("SELECT id, hstore(array['tag1', 'tag2'], array[tag1, tag2]), geom FROM table",
          "id", "hstore(array['tag1', 'tag2'], array[tag1, tag2])", "geom", "table",
          Optional.empty()),
      new TestQuery("SELECT id, hstore('tag', tag) as tags, geom FROM table",
          "id", "hstore('tag', tag)", "geom", "table",
          Optional.empty()),
      new TestQuery("SELECT id, tags, st_transform(geom, '1234') as geom FROM table",
          "id", "tags", "st_transform(geom, '1234')", "table",
          Optional.empty()),
      new TestQuery("SELECT id, a(b(c), d(e)), geom FROM table",
          "id", "a(b(c), d(e))", "geom", "table",
          Optional.empty()));

  @Test
  void parse() {
    for (TestQuery testQuery : testQueries) {
      Query q1 = QueryParser.parse(new Layer(), testQuery.query);
      assertEquals(q1.getId(), testQuery.id);
      assertEquals(q1.getTags(), testQuery.tags);
      assertEquals(q1.getGeom(), testQuery.geom);
      assertEquals(q1.getFrom(), testQuery.from);
      assertEquals(q1.getWhere(), testQuery.where);
    }
  }

}
