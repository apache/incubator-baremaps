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

package com.baremaps.tile.mbtiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sqlite.SQLiteDataSource;

class MBTilesTest extends TileStoreTest {

  @Override
  protected MBTiles createTileStore() throws Exception {
    File file = File.createTempFile("baremaps_", ".db");
    file.deleteOnExit();
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl("jdbc:sqlite:" + file.getPath());
    MBTiles tilesStore = new MBTiles(dataSource);
    tilesStore.initializeDatabase();
    return tilesStore;
  }

  @Test
  public void readWriteMetadata() throws Exception {
    MBTiles tileStore = createTileStore();
    Map<String, String> metadata = tileStore.readMetadata();
    assertTrue(metadata.size() == 0);

    Map<String, String> m1 = new HashMap<>();
    m1.put("test", "test");
    tileStore.writeMetadata(m1);

    Map<String, String> m2 = tileStore.readMetadata();
    assertTrue(m2.size() == 1);
    assertEquals(m2.get("test"), "test");
  }

}