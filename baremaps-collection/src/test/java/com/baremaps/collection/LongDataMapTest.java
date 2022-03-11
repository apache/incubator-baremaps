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

package com.baremaps.collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.collection.memory.OffHeapMemory;
import com.baremaps.collection.type.LongDataType;
import com.baremaps.collection.type.PairDataType;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LongDataMapTest {

  @ParameterizedTest
  @MethodSource("mapProvider")
  void test(LongDataMap<Long> map) {
    for (long i = 0; i < 1 << 20; i++) {
      map.put(i, i);
    }
    for (long i = 0; i < 1 << 20; i++) {
      assertEquals(i, map.get(i));
    }
  }

  static Stream<Arguments> mapProvider() {
    return Stream.of(
        Arguments.of(
            new LongDataOpenHashMap<>(new DataStore<>(new LongDataType(), new OffHeapMemory()))),
        Arguments.of(
            new LongAlignedDataSortedMap<>(
                new AlignedDataList<>(new LongDataType(), new OffHeapMemory()),
                new AlignedDataList<>(new LongDataType(), new OffHeapMemory()))),
        Arguments.of(
            new LongDataSortedMap<>(
                new AlignedDataList<>(
                    new PairDataType<>(new LongDataType(), new LongDataType()),
                    new OffHeapMemory()),
                new DataStore<>(new LongDataType(), new OffHeapMemory()))),
        Arguments.of(
            new LongAlignedDataSparseMap<>(
                new AlignedDataList<>(new LongDataType(), new OffHeapMemory()))),
        Arguments.of(new LongAlignedDataDenseMap<>(new LongDataType(), new OffHeapMemory())));
  }
}
