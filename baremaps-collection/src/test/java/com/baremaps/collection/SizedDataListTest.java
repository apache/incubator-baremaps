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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.collection.memory.Memory;
import com.baremaps.collection.memory.OffHeapMemory;
import com.baremaps.collection.type.LongDataType;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SizedDataListTest {

  @Test
  void segmentsTooSmall() {
    var dataType = new LongDataType();
    var memory = new OffHeapMemory(4);
    assertThrows(StoreException.class, () -> new AlignedDataList<>(dataType, memory));
  }

  @ParameterizedTest
  @MethodSource("com.baremaps.collection.memory.MemoryProvider#memories")
  void appendFixedSizeValues(Memory memory) throws IOException {
    var list = new AlignedDataList<>(new LongDataType(), memory);
    for (int i = 0; i < 1 << 10; i++) {
      assertEquals(i, list.add((long) i));
    }
    for (int i = 0; i < 1 << 10; i++) {
      assertEquals(i, list.get(i));
    }
    memory.close();
    memory.clean();
  }
}
