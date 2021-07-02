package com.baremaps.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class StreamUtilsTest {

  @Test
  void partition() {
    List<Integer> list = IntStream.range(0, 100).mapToObj(i -> i).collect(Collectors.toList());
    List<List<Integer>> partitions = StreamUtils.partition(list.stream(), 10)
        .map(stream -> stream.collect(Collectors.toList()))
        .collect(Collectors.toList());
    assertEquals(partitions.size(), 10);
  }
  
}