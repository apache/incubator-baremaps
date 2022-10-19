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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PostgresTileStoreTest {

  @Test
  void sameQueries() {
    List<PostgresQuery> queries =
        Arrays.asList(new PostgresQuery("a", 0, 20, "SELECT id, tags, geom FROM table"),
            new PostgresQuery("b", 0, 20, "SELECT id, tags, geom FROM table"));
    PostgresTileStore tileStore = new PostgresTileStore(null, queries);
    String query = tileStore.withQuery(new Tile(0, 0, 10));
    assertEquals(
        "with ha14cb45b as (select * from table where ((true) OR (true)) and st_intersects(geom, st_tileenvelope(10, 0, 0))) select st_asmvt(target, 'a', 4096, 'geom', 'id') from (select id as id, (tags ||  jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from ha14cb45b ) as target union all select st_asmvt(target, 'b', 4096, 'geom', 'id') from (select id as id, (tags ||  jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from ha14cb45b ) as target",
        query);
  }

  @Test
  void differentConditions1() {
    List<PostgresQuery> queries =
        Arrays.asList(new PostgresQuery("a", 0, 20, "SELECT id, tags, geom FROM table"),
            new PostgresQuery("b", 0, 20, "SELECT id, tags, geom FROM table WHERE condition = 1"));
    PostgresTileStore tileStore = new PostgresTileStore(null, queries);
    String query = tileStore.withQuery(new Tile(0, 0, 10));
    assertEquals(
        "with ha14cb45b as (select * from table where ((true) OR (condition = 1)) and st_intersects(geom, st_tileenvelope(10, 0, 0))) select st_asmvt(target, 'a', 4096, 'geom', 'id') from (select id as id, (tags ||  jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from ha14cb45b ) as target union all select st_asmvt(target, 'b', 4096, 'geom', 'id') from (select id as id, (tags ||  jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from ha14cb45b where condition = 1) as target",
        query);
  }

  @Test
  void differentConditions2() {
    List<PostgresQuery> queries = Arrays.asList(
        new PostgresQuery("a", 0, 20, "SELECT id, tags, geom FROM table WHERE condition = 1"),
        new PostgresQuery("b", 0, 20, "SELECT id, tags, geom FROM table WHERE condition = 2"));
    PostgresTileStore tileStore = new PostgresTileStore(null, queries);
    String query = tileStore.withQuery(new Tile(0, 0, 10));
    assertEquals(
        "with ha14cb45b as (select * from table where ((condition = 1) OR (condition = 2)) and st_intersects(geom, st_tileenvelope(10, 0, 0))) select st_asmvt(target, 'a', 4096, 'geom', 'id') from (select id as id, (tags ||  jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from ha14cb45b where condition = 1) as target union all select st_asmvt(target, 'b', 4096, 'geom', 'id') from (select id as id, (tags ||  jsonb_build_object('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from ha14cb45b where condition = 2) as target",
        query);
  }
}
