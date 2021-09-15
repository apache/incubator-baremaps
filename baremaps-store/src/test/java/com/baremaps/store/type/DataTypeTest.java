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

package com.baremaps.store.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Coordinate;

public class DataTypeTest {

  private static Stream<Arguments> memoryProvider() {
    return Stream.of(
        Arguments.of(new ByteDataType(), (byte) 1),
        Arguments.of(new ByteListDataType(), List.of((byte) 1, (byte) 2, (byte) 3)),
        Arguments.of(new DoubleDataType(), (double) 1),
        Arguments.of(new DoubleListDataType(), List.of((double) 1, (double) 2, (double) 3)),
        Arguments.of(new FloatDataType(), (float) 1),
        Arguments.of(new FloatListDataType(), List.of((float) 1, (float) 2, (float) 3)),
        Arguments.of(new IntDataType(), 1),
        Arguments.of(new IntListDataType(), List.of(1, 2, 3)),
        Arguments.of(new LongDataType(), (long) 1),
        Arguments.of(new LongListDataType(), List.of((long) 1, (long) 2, (long) 3)),
        Arguments.of(new ShortDataType(), (short) 1),
        Arguments.of(new ShortListDataType(), List.of((short) 1, (short) 2, (short) 3)),
        Arguments.of(new ListDataType<>(new IntDataType()), List.of(1, 2, 3)),
        Arguments.of(new CoordinateDataType(), new Coordinate(1, 2)),
        Arguments.of(new LonLatDataType(), new Coordinate(1, 2)));
  }

  @ParameterizedTest
  @MethodSource("memoryProvider")
  public void writeAndRead(DataType dataType, Object value) {
    var size = dataType.size(value);
    var buffer = ByteBuffer.allocate(size);
    dataType.write(buffer, 0, value);
    assertEquals(value, dataType.read(buffer, 0));
  }
}
