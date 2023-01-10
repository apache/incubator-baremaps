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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.apache.baremaps.collection.memory.OffHeapMemory;
import org.apache.baremaps.collection.store.AppendOnlyCollection;
import org.apache.baremaps.collection.store.MemoryAlignedDataStore;
import org.apache.baremaps.collection.type.LongDataType;
import org.apache.baremaps.collection.type.PairDataType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LongMapTest {

  @ParameterizedTest
  @MethodSource("mapProvider")
  void test(LongMap<Long> map) {
    for (long i = 0; i < 1 << 20; i++) {
      map.put(i, i);
    }
    for (long i = 0; i < 1 << 20; i++) {
      assertEquals(i, map.get(i));
    }
  }

  static Stream<Arguments> mapProvider() {
    return Stream
        .of(Arguments.of(
            new LongOpenHashMap<>(new AppendOnlyCollection<>(new LongDataType(), new OffHeapMemory()))),
            Arguments.of(new SortedLongFixedSizeDataMap<>(
                new MemoryAlignedDataStore<>(new LongDataType(), new OffHeapMemory()))),
            Arguments.of(new SortedLongVariableSizeDataMap<>(
                new MemoryAlignedDataStore<>(
                    new PairDataType<>(new LongDataType(), new LongDataType()),
                    new OffHeapMemory()),
                new AppendOnlyCollection<>(new LongDataType(), new OffHeapMemory()))),
            Arguments.of(new SparseLongFixedSizeDataMap<>(
                new MemoryAlignedDataStore<>(new LongDataType(), new OffHeapMemory()))),
            Arguments.of(
                new MemoryAlignedLongFixedSizeDataMap<>(new LongDataType(), new OffHeapMemory())));
  }
}
