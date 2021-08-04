package com.baremaps.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.model.Layer;
import com.baremaps.model.Query;
import com.baremaps.model.TileSet;
import com.baremaps.tile.Tile;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TilsetQueryParserTest {

  @Test
  void sameQueries() {
    Query q = new Query();
    q.setMinzoom(0);
    q.setMaxzoom(20);
    q.setSql("SELECT id, tags, geom FROM table");

    Layer a = new Layer();
    a.setId("a");
    a.setQueries(List.of(q));

    Layer b = new Layer();
    b.setId("b");
    b.setQueries(List.of(q));

    TileSet tileset = new TileSet();
    tileset.setLayers(Arrays.asList(a, b));

    TilesetQueryParser parser = new TilesetQueryParser();
    String query = parser.parse(tileset, new Tile(0, 0, 10));
    assertEquals("with ha14cb45b as (select id, (tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from (select id as id, tags as tags, geom as geom from table) as source where  st_intersects(geom, st_tileenvelope(10, 0, 0))) select st_asmvt(target, 'a', 4096, 'geom', 'id') from (select id, hstore_to_jsonb_loose(tags) as tags, geom from ha14cb45b ) as target union all select st_asmvt(target, 'b', 4096, 'geom', 'id') from (select id, hstore_to_jsonb_loose(tags) as tags, geom from ha14cb45b ) as target", query);
  }

  @Test
  void differentConditions() {
    Query q1 = new Query();
    q1.setMinzoom(0);
    q1.setMaxzoom(20);
    q1.setSql("SELECT id, tags, geom FROM table WHERE condition = 1");

    Layer a = new Layer();
    a.setId("a");
    a.setQueries(List.of(q1));

    Query q2 = new Query();
    q2.setMinzoom(0);
    q2.setMaxzoom(20);
    q2.setSql("SELECT id, tags, geom FROM table WHERE condition = 2");

    Layer b = new Layer();
    b.setId("b");
    b.setQueries(List.of(q2));

    TileSet tileset = new TileSet();
    tileset.setLayers(Arrays.asList(a, b));

    TilesetQueryParser parser = new TilesetQueryParser();
    String query = parser.parse(tileset, new Tile(0, 0, 10));
    assertEquals("with ha14cb45b as (select id, (tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', '')))) as tags, st_asmvtgeom(geom, st_tileenvelope(10, 0, 0), 4096, 256, true) as geom from (select id as id, tags as tags, geom as geom from table) as source where ((condition = 1) OR (condition = 2)) and st_intersects(geom, st_tileenvelope(10, 0, 0))) select st_asmvt(target, 'a', 4096, 'geom', 'id') from (select id, hstore_to_jsonb_loose(tags) as tags, geom from ha14cb45b where condition = 1) as target union all select st_asmvt(target, 'b', 4096, 'geom', 'id') from (select id, hstore_to_jsonb_loose(tags) as tags, geom from ha14cb45b where condition = 2) as target", query);
  }

}
