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

import java.util.stream.Stream;
import org.apache.baremaps.collection.type.LongDataType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DataListTest {

  @ParameterizedTest
  @MethodSource("listProvider")
  void addSetGet(DataList<Long> list) {
    for (long i = 0; i < 1000; i++) {
      list.add(i);
    }
    for (long i = 0; i < 1000; i++) {
      assertEquals(i, list.get(i));
    }
    for (long i = 0; i < 1000; i++) {
      list.set(i, i + 1);
    }
    for (long i = 0; i < 1000; i++) {
      assertEquals(i + 1, list.get(i));
    }
  }

  @ParameterizedTest
  @MethodSource("listProvider")
  void containsValue(DataList<Long> list) {
    for (long i = 0; i < 1000; i++) {
      assertFalse(list.contains(i));
    }
    for (long i = 0; i < 1000; i++) {
      list.add(i);
    }
    for (long i = 0; i < 1000; i++) {
      assertTrue(list.contains(i));
    }
  }

  @ParameterizedTest
  @MethodSource("listProvider")
  void clear(DataList<Long> list) {
    for (long i = 0; i < 1000; i++) {
      list.add(i);
    }
    list.clear();
    assertEquals(0, list.size());
    for (long i = 0; i < 1000; i++) {
      assertFalse(list.contains(i));
    }
  }


  static Stream<Arguments> listProvider() {
    return Stream.of(
        Arguments.of(new FixedSizeDataList<>(new LongDataType())),
        Arguments.of(new IndexedDataList<>(new MemoryAlignedDataList<>(new LongDataType()),
            new AppendOnlyBuffer<>(new LongDataType()))),
        Arguments.of(new MemoryAlignedDataList<>(new LongDataType())));
  }
}
