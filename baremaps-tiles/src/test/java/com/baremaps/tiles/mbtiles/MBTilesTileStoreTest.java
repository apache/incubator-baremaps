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

package com.baremaps.tiles.mbtiles;

import com.baremaps.tiles.TileStore;
import com.baremaps.tiles.TileStoreTest;
import java.io.File;
import org.sqlite.SQLiteDataSource;

class MBTilesTileStoreTest extends TileStoreTest {

  @Override
  protected TileStore createTileStore() throws Exception {
    File file = File.createTempFile("baremaps_", ".db");
    file.deleteOnExit();
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl("jdbc:sqlite:" + file.getPath());
    MBTilesTileStore tilesStore = new MBTilesTileStore(dataSource);
    tilesStore.initializeDatabase();
    return tilesStore;
  }

}