/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.apache.baremaps.data.collection.DataCollectionException;
import org.apache.baremaps.data.collection.FixedSizeDataList;
import org.apache.baremaps.data.memory.Memory;
import org.apache.baremaps.data.memory.OffHeapMemory;
import org.apache.baremaps.data.type.LongDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SizedDataListTest {

  @Test
  void segmentsTooSmall() {
    var dataType = new LongDataType();
    var memory = new OffHeapMemory(4);
    assertThrows(DataCollectionException.class, () -> FixedSizeDataList.<Long>builder()
            .dataType(dataType)
            .memory(memory)
            .build());
  }

  @ParameterizedTest
  @MethodSource("org.apache.baremaps.data.memory.MemoryProvider#memories")
  void appendFixedSizeValues(Memory memory) throws IOException {
    var list = FixedSizeDataList.<Long>builder()
            .dataType(new LongDataType())
            .memory(memory)
            .build();
    for (int i = 0; i < 1 << 10; i++) {
      assertEquals(i, list.addIndexed((long) i));
    }
    for (int i = 0; i < 1 << 10; i++) {
      assertEquals(i, list.get(i));
    }
    memory.close();
    memory.clear();
  }
}
