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
package com.baremaps.osm.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.osm.cache.Cache.Entry;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class InMemoryCacheTest {

  @Test
  void test() throws Exception {
    Cache cache = new InMemoryCache<Long, Coordinate>();
    Coordinate c1 = new Coordinate(1, 0);
    Coordinate c2 = new Coordinate(2, 0);
    Coordinate c3 = new Coordinate(3, 0);
    Coordinate c4 = new Coordinate(4, 0);
    cache.put(1, c1);
    cache.putAll(Arrays.asList(
        new Entry(2, c2),
        new Entry(3, c3),
        new Entry(4, c4)));
    assertEquals(cache.get(1), c1);
    assertEquals(cache.getAll(Arrays.asList(1, 2)), Arrays.asList(c1, c2));
    cache.delete(1);
    assertEquals(cache.get(1), null);
    cache.deleteAll(Arrays.asList(1, 2));
    assertEquals(cache.getAll(Arrays.asList(1, 2)), Arrays.asList(null, null));
  }
}