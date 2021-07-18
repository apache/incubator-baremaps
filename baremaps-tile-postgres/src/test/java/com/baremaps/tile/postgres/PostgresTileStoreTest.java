package com.baremaps.tile.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.config.tileset.Layer;
import com.baremaps.config.tileset.Query;
import com.baremaps.config.tileset.Tileset;
import com.baremaps.tile.Tile;
import org.junit.jupiter.api.Test;

class PostgresTileStoreTest {

  @Test
  void query() {
    Tileset tileset = new Tileset(
        new Layer("a", new Query(0, 20, "SELECT id, tags, geom FROM table WHERE condition = 1")),
        new Layer("b", new Query(0, 20, "SELECT id, tags, geom FROM table WHERE condition = 2"))
    );
    PostgresTileStore tileStore = new PostgresTileStore(null, tileset);
    String query = tileStore.withQuery(new Tile(0, 0, 10));
    assertEquals("with ha14cb45b as (select id, (tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'st_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from (select id as id, tags as tags, geom as geom from table) as source where ((condition = 1) or (condition = 2)) and st_intersects(geom, st_tileenvelope(10, 0, 0))) select st_asmvt(target, 'a', 4096) from (select id, hstore_to_jsonb_loose(tags), geom from ha14cb45b where condition = 1) as target union all select st_asmvt(target, 'b', 4096) from (select id, hstore_to_jsonb_loose(tags), geom from ha14cb45b where condition = 2) as target", query.toLowerCase());
  }

}