package com.baremaps.store.memory;

import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public class MemoryProvider {

  public static final int SEGMENT_BYTES = 1 << 10;

  public static Stream<Arguments> memories() throws IOException {
    return Stream.of(
        Arguments.of(new OnHeapMemory(SEGMENT_BYTES)),
        Arguments.of(new OffHeapMemory(SEGMENT_BYTES)),
        Arguments.of(new OnDiskMemory(SEGMENT_BYTES)));
  }

}
