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

package com.baremaps.store.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MemoryTest {

  @ParameterizedTest
  @MethodSource("memoryProvider")
  public void capacity(Memory memory) {
    assertEquals(1 << 10, memory.segmentBytes());
  }

  @ParameterizedTest
  @MethodSource("memoryProvider")
  public void segment(Memory memory) {
    for (int i = 0; i < 10; i++) {
      assertEquals(1 << 10, memory.segment(i).capacity());
      assertSame(memory.segment(i), memory.segment(i));
      assertNotSame(memory.segment(i), memory.segment(i + 1));
    }
  }

  private static Stream<Arguments> memoryProvider() throws IOException {
    return Stream.of(
        Arguments.of(new OnHeapMemory(1 << 10)),
        Arguments.of(new OffHeapMemory(1 << 10)),
        Arguments.of(new FileMemory(1 << 10)),
        Arguments.of(new DirectoryMemory(1 << 10)));
  }
}
