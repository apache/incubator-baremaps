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

package org.apache.baremaps.tilestore.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.vectortile.tileset.Tileset;
import org.apache.baremaps.vectortile.tileset.TilesetLayer;
import org.apache.baremaps.vectortile.tileset.TilesetQuery;
import org.junit.jupiter.api.Test;

class PostgresTileStoreTest {

  @Test
  void sameQueries() {
    Tileset tileset = new Tileset();
    tileset.setMinzoom(0);
    tileset.setMaxzoom(20);
    tileset.setVectorLayers(List.of(
        new TilesetLayer("a", Map.of(), "", 0, 20,
            List.of(new TilesetQuery(0, 20, "SELECT id, tags, geom FROM table"))),
        new TilesetLayer("b", Map.of(), "", 0, 20,
            List.of(new TilesetQuery(0, 20, "SELECT id, tags, geom FROM table")))));
    PostgresTileStore tileStore = new PostgresTileStore(null, tileset);
    String query = tileStore.withQuery(new TileCoord(0, 0, 10));
    assertEquals(
        """
            SELECT ((WITH mvtgeom AS (
            SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(10, 0, 0)) AS geom, t.tags, t.id
            FROM (SELECT id, tags, geom FROM table) AS t
            WHERE t.geom && ST_TileEnvelope(10, 0, 0, margin => (64.0/4096))
            ) SELECT ST_AsMVT(mvtgeom.*, 'a') FROM mvtgeom
            ) || (WITH mvtgeom AS (
            SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(10, 0, 0)) AS geom, t.tags, t.id
            FROM (SELECT id, tags, geom FROM table) AS t
            WHERE t.geom && ST_TileEnvelope(10, 0, 0, margin => (64.0/4096))
            ) SELECT ST_AsMVT(mvtgeom.*, 'b') FROM mvtgeom
            )) mvtTile""",
        query);
  }

  @Test
  void differentConditions1() {
    Tileset tileset = new Tileset();
    tileset.setMinzoom(0);
    tileset.setMaxzoom(20);
    tileset.setVectorLayers(List.of(
        new TilesetLayer("a", Map.of(), "", 0, 20,
            List.of(new TilesetQuery(0, 20, "SELECT id, tags, geom FROM table"))),
        new TilesetLayer("b", Map.of(), "", 0, 20, List
            .of(new TilesetQuery(0, 20, "SELECT id, tags, geom FROM table WHERE condition = 1")))));
    PostgresTileStore tileStore = new PostgresTileStore(null, tileset);
    String query = tileStore.withQuery(new TileCoord(0, 0, 10));
    assertEquals("""
        SELECT ((WITH mvtgeom AS (
        SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(10, 0, 0)) AS geom, t.tags, t.id
        FROM (SELECT id, tags, geom FROM table) AS t
        WHERE t.geom && ST_TileEnvelope(10, 0, 0, margin => (64.0/4096))
        ) SELECT ST_AsMVT(mvtgeom.*, 'a') FROM mvtgeom
        ) || (WITH mvtgeom AS (
        SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(10, 0, 0)) AS geom, t.tags, t.id
        FROM (SELECT id, tags, geom FROM table WHERE condition = 1) AS t
        WHERE t.geom && ST_TileEnvelope(10, 0, 0, margin => (64.0/4096))
        ) SELECT ST_AsMVT(mvtgeom.*, 'b') FROM mvtgeom
        )) mvtTile""",
        query);
  }

  @Test
  void differentConditions2() {
    Tileset tileset = new Tileset();
    tileset.setMinzoom(0);
    tileset.setMaxzoom(20);
    tileset.setVectorLayers(List.of(
        new TilesetLayer("a", Map.of(), "", 0, 20,
            List.of(
                new TilesetQuery(0, 20, "SELECT id, tags, geom FROM table WHERE condition = 1"))),
        new TilesetLayer("b", Map.of(), "", 0, 20, List
            .of(new TilesetQuery(0, 20, "SELECT id, tags, geom FROM table WHERE condition = 2")))));
    PostgresTileStore tileStore = new PostgresTileStore(null, tileset);
    String query = tileStore.withQuery(new TileCoord(0, 0, 10));
    assertEquals(
        """
            SELECT ((WITH mvtgeom AS (
            SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(10, 0, 0)) AS geom, t.tags, t.id
            FROM (SELECT id, tags, geom FROM table WHERE condition = 1) AS t
            WHERE t.geom && ST_TileEnvelope(10, 0, 0, margin => (64.0/4096))
            ) SELECT ST_AsMVT(mvtgeom.*, 'a') FROM mvtgeom
            ) || (WITH mvtgeom AS (
            SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(10, 0, 0)) AS geom, t.tags, t.id
            FROM (SELECT id, tags, geom FROM table WHERE condition = 2) AS t
            WHERE t.geom && ST_TileEnvelope(10, 0, 0, margin => (64.0/4096))
            ) SELECT ST_AsMVT(mvtgeom.*, 'b') FROM mvtgeom
            )) mvtTile""",
        query);
  }
}
