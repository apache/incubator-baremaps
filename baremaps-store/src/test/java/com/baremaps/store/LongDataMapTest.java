package com.baremaps.store;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.type.LongDataType;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LongDataMapTest {

  @ParameterizedTest
  @MethodSource("mapProvider")
  public void test(LongDataMap<Long> value) {
    LongFixedSizeDataSparseMap<Long> map = new LongFixedSizeDataSparseMap<>(
        new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory()));
    for (long i = 0; i < 1 << 20; i++) {
      map.put(i, i);
    }
    for (long i = 0; i < 1 << 20; i++) {
      assertEquals(i, map.get(i));
    }
  }

  private static Stream<Arguments> mapProvider() throws IOException {
    return Stream.of(
        Arguments.of(new LongDataOpenHashMap<>(
            new DataStore<>(new LongDataType(), new OffHeapMemory()))),
        Arguments.of(new LongDataSortedMap<>(
            new DataStore<>(new LongDataType(), new OffHeapMemory()))),
        Arguments.of(new LongFixedSizeDataSortedMap<>(
            new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory()),
            new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory()))),
        Arguments.of(
            new LongFixedSizeDataSparseMap<>(new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory()))),
        Arguments.of(
            new LongFixedSizeDataDenseMap<>(new LongDataType(), new OffHeapMemory()))
    );
  }

}