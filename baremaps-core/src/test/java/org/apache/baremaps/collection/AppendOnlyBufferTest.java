/*
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

package org.apache.baremaps.collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Random;
import org.apache.baremaps.collection.memory.OffHeapMemory;
import org.apache.baremaps.collection.type.DataType;
import org.apache.baremaps.collection.type.IntegerDataType;
import org.apache.baremaps.collection.type.IntegerListDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AppendOnlyBufferTest {

  @Test
  void addFixedSizeData() {
    var collection = new AppendOnlyBuffer<>(new IntegerDataType(), new OffHeapMemory(1 << 10));
    for (int i = 0; i < 1 << 20; i++) {
      assertEquals(Long.BYTES + (i << 2), collection.addPositioned(i));
    }
    for (int i = 0; i < 1 << 20; i++) {
      assertEquals(i, collection.read(Long.BYTES + (i << 2)));
    }
  }

  @Test
  void addVariableSizeValues() {
    var collection = new AppendOnlyBuffer<>(new IntegerListDataType(), new OffHeapMemory(1 << 10));
    var random = new Random(0);
    var positions = new ArrayList<Long>();
    var values = new ArrayList<ArrayList<Integer>>();
    for (int i = 0; i < 1 << 20; i++) {
      var size = random.nextInt(10);
      var value = new ArrayList<Integer>();
      for (int j = 0; j < size; j++) {
        value.add(random.nextInt(1 << 20));
      }
      positions.add(collection.addPositioned(value));
      values.add(value);
    }
    for (int i = 0; i < positions.size(); i++) {
      var value = collection.read(positions.get(i));
      assertEquals(values.get(i), value);
    }
  }

  @ParameterizedTest
  @MethodSource("org.apache.baremaps.collection.type.DataTypeProvider#dataTypes")
  void testAllDataTypes(DataType dataType, Object value) {
    var num = 1 << 10;
    var collection = new AppendOnlyBuffer<>(dataType, new OffHeapMemory(1 << 8));

    // write values
    for (int i = 0; i < num; i++) {
      collection.addPositioned(value);
    }
    collection.close();

    // read values
    int count = 0;
    for (var v : collection) {
      assertEquals(value, v);
      count++;
    }

    // check count
    assertEquals(num, count);
  }
}
