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

import java.util.*;
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
    for (long i = 0; i < 1000; i++) {
      map.put(i, i * 2);
    }
    for (long i = 0; i < 1000; i++) {
      assertEquals(i * 2, map.get(i));
    }
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void containsKey(DataMap<Long> map) {
    for (long i = 0; i < 1000; i++) {
      assertFalse(map.containsKey(i));
    }
    for (long i = 0; i < 1000; i++) {
      map.put(i, i * 2);
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
      map.put(i, i * 2);
    }
    for (long i = 0; i < 1000; i++) {
      assertTrue(map.containsValue(i * 2));
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
    for (long i = 0; i < 1000; i++) {
      set.add(i);
      map.put(i, i);
    }
    var res = map.keySet();
    assertEquals(set, res);
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void valueSet(DataMap<Long> map) {
    var set = new HashSet<Long>();
    for (long i = 0; i < 1000; i++) {
      set.add(i);
      map.put(i, i);
    }
    assertEquals(set, new HashSet(map.values()));
  }

  @ParameterizedTest
  @MethodSource("mapProvider")
  void entrySet(DataMap<Long> map) {
    var set = new HashSet<Entry<Long, Long>>();
    for (long i = 0; i < 1000; i++) {
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

  @ParameterizedTest
  @MethodSource("mapProvider")
  void map(DataMap<Long> map) {
    assertTrue(map.isEmpty());

    map.put(10l, 10l);
    map.put(15l, 15l);
    map.put(20l, 20l);

    assertFalse(map.isEmpty());

    assertEquals(3l, map.size());

    assertEquals(10l, map.get(10l));
    assertEquals(15l, map.get(15l));
    assertEquals(20l, map.get(20l));

    assertEquals(Set.of(10l, 15l, 20l), map.keySet());;
  }


  static Stream<Arguments> mapProvider() {
    return Stream
        .of(
            Arguments.of(
                new IndexedDataMap<>(
                    new AppendOnlyBuffer<>(new LongDataType(), new OffHeapMemory()))),
            Arguments.of(new MonotonicFixedSizeDataMap<>(
                new MemoryAlignedDataList<>(new LongDataType(), new OffHeapMemory()))),
            Arguments.of(new MonotonicDataMap<>(
                new MemoryAlignedDataList<>(
                    new PairDataType<>(new LongDataType(), new LongDataType()),
                    new OffHeapMemory()),
                new AppendOnlyBuffer<>(new LongDataType(), new OffHeapMemory()))),
            Arguments.of(new MonotonicPairedDataMap<>(new MemoryAlignedDataList<>(
                new PairDataType<>(new LongDataType(), new LongDataType())))));
  }
}
