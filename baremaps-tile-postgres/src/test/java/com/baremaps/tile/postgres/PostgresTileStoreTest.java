package com.baremaps.tile.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.tile.Tile;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PostgresTileStoreTest {

  @Test
  void sameQueries() {
    List<PostgresQuery> queries = Arrays.asList(
        new PostgresQuery("a", 0, 20, "SELECT id, tags, geom FROM table"),
        new PostgresQuery("b", 0, 20, "SELECT id, tags, geom FROM table")
    );
    PostgresTileStore tileStore = new PostgresTileStore(null, queries);
    String query = tileStore.withQuery(new Tile(0, 0, 10));
    assertEquals(
        "with ha14cb45b as (select id, (tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from (select id as id, tags as tags, geom as geom from table) as source where  st_intersects(geom, st_tileenvelope(10, 0, 0))) select st_asmvt(target, 'a', 4096, 'geom', 'id') from (select id, hstore_to_jsonb_loose(tags) as tags, geom from ha14cb45b ) as target union all select st_asmvt(target, 'b', 4096, 'geom', 'id') from (select id, hstore_to_jsonb_loose(tags) as tags, geom from ha14cb45b ) as target",
        query);
  }

  @Test
  void differentConditions() {
    List<PostgresQuery> queries = Arrays.asList(
        new PostgresQuery("a", 0, 20, "SELECT id, tags, geom FROM table WHERE condition = 1"),
        new PostgresQuery("b", 0, 20, "SELECT id, tags, geom FROM table WHERE condition = 2")
    );
    PostgresTileStore tileStore = new PostgresTileStore(null, queries);
    String query = tileStore.withQuery(new Tile(0, 0, 10));
    assertEquals(
        "with ha14cb45b as (select id, (tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from (select id as id, tags as tags, geom as geom from table) as source where ((condition = 1) OR (condition = 2)) and st_intersects(geom, st_tileenvelope(10, 0, 0))) select st_asmvt(target, 'a', 4096, 'geom', 'id') from (select id, hstore_to_jsonb_loose(tags) as tags, geom from ha14cb45b where condition = 1) as target union all select st_asmvt(target, 'b', 4096, 'geom', 'id') from (select id, hstore_to_jsonb_loose(tags) as tags, geom from ha14cb45b where condition = 2) as target",
        query);
  }

}