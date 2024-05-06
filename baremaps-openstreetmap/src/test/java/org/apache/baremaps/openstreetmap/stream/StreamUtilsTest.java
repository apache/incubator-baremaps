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

package org.apache.baremaps.openstreetmap.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class StreamUtilsTest {

  @Test
  void partition() {
    List<Integer> list = IntStream.range(0, 100).boxed().toList();
    List<List<Integer>> partitions = StreamUtils.partition(list.stream(), 10).toList();
    assertEquals(partitions.size(), 10);
  }

  @Test
  void bufferInSourceOrder() {
    List<Integer> l1 = IntStream.range(0, 100).boxed().toList();
    List<Integer> l2 = StreamUtils.bufferInSourceOrder(l1.stream(), i -> i, 10).toList();
    assertEquals(l2.size(), l1.size());
    assertEquals(l2, l1);
  }

  @Test
  void bufferInSourceOrderWithException() {
    assertThrows(StreamException.class, () -> {
      List<Integer> l1 = IntStream.range(0, 100).boxed().toList();
      Function<Integer, Integer> throwException = i -> {
        throw new RuntimeException();
      };
      StreamUtils.bufferInSourceOrder(l1.stream(), throwException, 10).sorted().toList();
    });
  }

  @Test
  void bufferInCompletionOrder() {
    List<Integer> l1 = IntStream.range(0, 100).boxed().toList();
    List<Integer> l2 =
        StreamUtils.bufferInCompletionOrder(l1.stream(), i -> i, 10).sorted().toList();
    assertEquals(l2.size(), l1.size());
    assertEquals(l2, l1);
  }

  @Test
  void bufferInCompletionOrderWithException() {
    assertThrows(StreamException.class, () -> {
      List<Integer> l1 = IntStream.range(0, 100).boxed().toList();
      Function<Integer, Integer> throwException = i -> {
        throw new RuntimeException();
      };
      StreamUtils.bufferInCompletionOrder(l1.stream(), throwException, 10).sorted().toList();
    });
  }

}
