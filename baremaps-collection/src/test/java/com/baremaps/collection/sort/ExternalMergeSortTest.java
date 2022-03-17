package com.baremaps.collection.sort;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baremaps.collection.AlignedDataList;
import com.baremaps.collection.DataList;
import com.baremaps.collection.memory.OnHeapMemory;
import com.baremaps.collection.type.LongDataType;
import java.io.IOException;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class ExternalMergeSortTest {

  @Test
  void sort() throws IOException {
    AlignedDataList<Long> input = new AlignedDataList<>(new LongDataType(), new OnHeapMemory());
    Random random = new Random(0);
    for (long i = 0; i < 1000000; i++) {
      input.add(random.nextLong());
    }
    Supplier<DataList<Long>> tempListSupplier = () -> new AlignedDataList<>(new LongDataType(), new OnHeapMemory());
    AlignedDataList<Long> output = new AlignedDataList<>(new LongDataType(), new OnHeapMemory());
    new ExternalMergeSort(tempListSupplier).sort(input, output, Comparator.naturalOrder());
    long v = output.get(0);
    for (long i = 0; i < output.size(); i++) {
      assertTrue(output.get(i) >= v);
      v = output.get(i);
    }
    assertEquals(1000000, output.size());
  }
}