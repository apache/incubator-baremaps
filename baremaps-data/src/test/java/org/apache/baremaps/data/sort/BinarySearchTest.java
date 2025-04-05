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

import static org.junit.jupiter.api.Assertions.*;

import org.apache.baremaps.data.algorithm.BinarySearch;
import org.apache.baremaps.data.collection.MemoryAlignedDataList;
import org.apache.baremaps.data.type.LongDataType;
import org.junit.jupiter.api.Test;

class BinarySearchTest {

  @Test
  void binarySearch() {
    var list = MemoryAlignedDataList.<Long>builder()
        .dataType(new LongDataType())
        .build();
    for (long i = 0; i < 1000; i++) {
      list.add(i);
    }
    for (long i = 0; i < 1000; i++) {
      assertEquals(i, BinarySearch.binarySearch(list, i, Long::compare));
    }
  }
}
