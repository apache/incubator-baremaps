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

package com.baremaps.osm.lmdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.baremaps.osm.cache.Cache.Entry;
import com.baremaps.osm.cache.CoordinateCache;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lmdbjava.Env;
import org.locationtech.jts.geom.Coordinate;

class LmdbCoordinateCacheTest {

  @Test
  @Tag("integration")
  void test() throws Exception {
    Path path = Files.createTempDirectory(Paths.get("."), "baremaps_").toAbsolutePath();
    Env<ByteBuffer> env =
        Env.create().setMapSize(1_000_000_000_000L).setMaxDbs(3).open(path.toFile());
    CoordinateCache cache = new LmdbCoordinateCache(env);
    Coordinate c1 = new Coordinate(1, 0);
    Coordinate c2 = new Coordinate(2, 0);
    Coordinate c3 = new Coordinate(3, 0);
    Coordinate c4 = new Coordinate(4, 0);
    cache.add(1l, c1);
    cache.add(Arrays.asList(new Entry(2l, c2), new Entry(3l, c3), new Entry(4l, c4)));
    assertEquals(cache.get(1l), c1);
    assertEquals(cache.get(Arrays.asList(1l, 2l)), Arrays.asList(c1, c2));
    cache.delete(1l);
    assertNull(cache.get(1l));
    cache.deleteAll(Arrays.asList(1l, 2l));
    assertEquals(Arrays.asList(null, null), cache.get(Arrays.asList(1l, 2l)));
  }
}
