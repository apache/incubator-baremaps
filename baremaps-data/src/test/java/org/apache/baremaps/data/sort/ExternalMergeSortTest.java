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

package org.apache.baremaps.data.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import org.apache.baremaps.data.algorithm.ExternalMergeSort;
import org.apache.baremaps.data.collection.AppendOnlyLog;
import org.apache.baremaps.data.collection.DataList;
import org.apache.baremaps.data.collection.IndexedDataList;
import org.apache.baremaps.data.collection.MemoryAlignedDataList;
import org.apache.baremaps.data.memory.OnHeapMemory;
import org.apache.baremaps.data.type.LongDataType;
import org.apache.baremaps.data.type.StringDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExternalMergeSortTest {

  List<String> strings;
  List<String> stringsAsc;
  List<String> stringsDsc;
  List<String> stringsDistinct;
  Supplier<DataList<String>> supplier;
  DataList<String> input;
  DataList<String> output;

  @BeforeEach
  void before() {
    strings = List.of("a", "b", "k", "c", "d", "a", "i", "j", "e", "e", "h", "f", "g");
    stringsAsc = strings.stream().sorted(Comparator.naturalOrder()).toList();
    stringsDsc = strings.stream().sorted(Comparator.reverseOrder()).toList();
    stringsDistinct = stringsAsc.stream().distinct().toList();
    supplier = () -> IndexedDataList.<String>builder()
        .index(MemoryAlignedDataList.<Long>builder()
            .dataType(new LongDataType())
            .memory(new OnHeapMemory())
            .build())
        .values(AppendOnlyLog.<String>builder()
            .dataType(new StringDataType())
            .memory(new OnHeapMemory())
            .build())
        .build();
    input = supplier.get();
    output = supplier.get();
    for (var string : strings) {
      input.addIndexed(string);
    }
  }

  public List<String> stringList(DataList<String> list) {
    var l = new ArrayList<String>();
    for (long i = 0; i < list.size(); i++) {
      l.add(list.get(i));
    }
    return l;
  }

  public String randomString(Random random) {
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 10;
    StringBuilder buffer = new StringBuilder(targetStringLength);
    for (int i = 0; i < 8 + random.nextInt(248); i++) {
      int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
      buffer.append((char) randomLimitedInt);
    }
    return buffer.toString();
  }

  @Test
  void sortStringsAsc() throws IOException {
    ExternalMergeSort.sort(input, output, Comparator.naturalOrder(), supplier, 4, false, true);
    assertEquals(stringsAsc, stringList(output));
  }

  @Test
  void sortStringsDsc() throws IOException {
    ExternalMergeSort.sort(input, output, Comparator.reverseOrder(), supplier, 4, false, true);
    assertEquals(stringsDsc, stringList(output));
  }

  @Test
  void sortStringsDistinct() throws IOException {
    ExternalMergeSort.sort(input, output, Comparator.naturalOrder(), supplier, 4, true, true);
    assertEquals(stringsDistinct, stringList(output));
  }

  @Test
  void sortRandomString() throws IOException {
    var random = new Random(0);
    for (int i = 0; i < 1_000_000; i++) {
      input.addIndexed(randomString(random));
    }
    ExternalMergeSort.sort(input, output, Comparator.naturalOrder(), supplier, 100_000, false,
        true);
    for (int i = 1; i < 1_000_000; i++) {
      assertTrue(output.get(i - 1).compareTo(output.get(i)) <= 0);
    }
  }
}
