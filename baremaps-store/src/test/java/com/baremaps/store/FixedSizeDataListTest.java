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

package com.baremaps.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.type.FixedSizeDataType;
import com.baremaps.store.type.IntDataType;
import com.baremaps.store.type.LongDataType;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class FixedSizeDataListTest {

  @Test
  public void smallSegments() {
    assertThrows(
        RuntimeException.class,
        () -> new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory(4)));
  }

  @Test
  public void misalignedSegments() {
    assertThrows(
        RuntimeException.class,
        () -> {
          new FixedSizeDataList<>(
              new FixedSizeDataType<>() {
                @Override
                public int size(Object value) {
                  return 3;
                }

                @Override
                public void write(ByteBuffer buffer, int position, Object value) {}

                @Override
                public Object read(ByteBuffer buffer, int position) {
                  return null;
                }
              },
              new OffHeapMemory(16));
        });
  }

  @Test
  public void appendFixedSizeValues() {
    var list = new FixedSizeDataList<>(new IntDataType(), new OffHeapMemory(1 << 10));
    for (int i = 0; i < 1 << 20; i++) {
      assertEquals(i, list.add(i));
    }
    for (int i = 0; i < 1 << 20; i++) {
      assertEquals(i, list.get(i));
    }
  }
}
