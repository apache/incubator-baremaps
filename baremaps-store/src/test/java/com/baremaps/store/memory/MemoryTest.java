package com.baremaps.store.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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