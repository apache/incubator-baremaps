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

package org.apache.baremaps.collection;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.apache.baremaps.collection.memory.OffHeapMemory;
import org.apache.baremaps.collection.type.LongDataType;
import org.apache.baremaps.collection.type.PairDataType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DataMapTest {

  @ParameterizedTest
  @MethodSource("mapProvider")
  void putAndGet(DataMap<Long> map) {
    for (long i = 0; i < 1 << 20; i++) {
      map.put(i, i);
    }
    for (long i = 0; i < 1 << 20; i++) {
      assertEquals(i, map.get(i));
    }
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void containsKey(DataMap<Long> map) {
    for (long i = 0; i < 1000; i++) {
      assertFalse(map.containsKey(i));
    }
    for (long i = 0; i < 1000; i++) {
      map.put(i, i);
    }
    for (long i = 0; i < 1000; i++) {
      assertTrue(map.containsKey(i));
    }
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void containsValue(DataMap<Long> map) {
    for (long i = 0; i < 1000; i++) {
      assertFalse(map.containsValue(i));
    }
    for (long i = 0; i < 1000; i++) {
      map.put(i, i);
    }
    for (long i = 0; i < 1000; i++) {
      assertTrue(map.containsValue(i));
    }
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void getAll(DataMap<Long> map) {
    for (long i = 0; i < 1000; i++) {
      map.put(i, i);
    }
    var keys = List.of(0l, 10l, 100l, 1000l);
    var vals = new ArrayList<>(keys.size());
    vals.add(0l);
    vals.add(10l);
    vals.add(100l);
    vals.add(null);
    assertEquals(vals, map.getAll(keys));
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void size(DataMap<Long> map) {
    for (long i = 0; i < 1000; i++) {
      map.put(i, i);
    }
    for (long i = 0; i < 1000; i++) {
      assertEquals(1000, map.size());
    }
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void keySet(DataMap<Long> map) {
    var set = new HashSet<Long>();
    for (long i = 0; i < 10; i++) {
      set.add(i);
      map.put(i, i);
    }
    assertEquals(set, map.values());
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void valueSet(DataMap<Long> map) {
    var set = new HashSet<Long>();
    for (long i = 0; i < 10; i++) {
      set.add(i);
      map.put(i, i);
    }
    assertEquals(set, map.values());
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void entrySet(DataMap<Long> map) {
    var set = new HashSet<Entry<Long, Long>>();
    for (long i = 0; i < 10; i++) {
      set.add(Map.entry(i, i));
      map.put(i, i);
    }
    assertEquals(set, map.entrySet());
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void isEmpty(DataMap<Long> map) {
    assertTrue(map.isEmpty());
    map.put(0l, 0l);
    assertFalse(map.isEmpty());
  }

  static Stream<Arguments> mapProvider() {
    return Stream
        .of(Arguments.of(
            new IndexedDataMap<>(new AppendOnlyBuffer<>(new LongDataType(), new OffHeapMemory()))),
            Arguments.of(new MonotonicFixedSizeDataMap<>(
                new MemoryAlignedDataList<>(new LongDataType(), new OffHeapMemory()))),
            Arguments.of(new MonotonicDataMap<>(
                new AppendOnlyBuffer<>(new LongDataType(), new OffHeapMemory()),
                new MemoryAlignedDataList<>(
                    new PairDataType<>(new LongDataType(), new LongDataType()),
                    new OffHeapMemory()))),
            Arguments.of(new MonotonicSparseDataMap<>(
                new MemoryAlignedDataList<>(new LongDataType(), new OffHeapMemory()))));
  }
}
