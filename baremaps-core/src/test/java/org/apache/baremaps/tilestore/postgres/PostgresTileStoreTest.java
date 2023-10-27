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
import org.apache.baremaps.vectortile.tileset.Tileset;
import org.apache.baremaps.vectortile.tileset.TilesetLayer;
import org.apache.baremaps.vectortile.tileset.TilesetQuery;
import org.junit.jupiter.api.Test;

class PostgresTileStoreTest {

  @Test
  void prepareQuery() {
    var tileset = new Tileset();
    tileset.setMinzoom(0);
    tileset.setMaxzoom(20);
    tileset.setVectorLayers(List.of(
        new TilesetLayer("a", Map.of(), "", 0, 20,
            List.of(new TilesetQuery(0, 20, "SELECT id, tags, geom FROM table"))),
        new TilesetLayer("b", Map.of(), "", 0, 20,
            List.of(new TilesetQuery(0, 20, "SELECT id, tags, geom FROM table")))));
    var query = PostgresTileStore.prepareQuery(tileset, 10);
    assertEquals(
        "SELECT ((WITH mvtGeom AS (SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(?, ?, ?)) AS geom, t.tags, t.id FROM (SELECT id, tags, geom FROM table) AS t WHERE t.geom && ST_TileEnvelope(?, ?, ?, margin => (64.0/4096))) SELECT ST_AsMVT(mvtGeom.*, 'a') FROM mvtGeom) || (WITH mvtGeom AS (SELECT ST_AsMVTGeom(t.geom, ST_TileEnvelope(?, ?, ?)) AS geom, t.tags, t.id FROM (SELECT id, tags, geom FROM table) AS t WHERE t.geom && ST_TileEnvelope(?, ?, ?, margin => (64.0/4096))) SELECT ST_AsMVT(mvtGeom.*, 'b') FROM mvtGeom)) mvtTile",
        query.sql());
  }
}
