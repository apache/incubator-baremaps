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

package org.apache.baremaps.collection.type;



import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.locationtech.jts.geom.Coordinate;

public class DataTypeProvider {

  private static Stream<Arguments> dataTypes() {
    return Stream.of(Arguments.of(new StringDataType(), "Hello, World!"),
        Arguments.of(new ByteDataType(), (byte) 1),
        Arguments.of(new ByteListDataType(), List.of((byte) 1, (byte) 2, (byte) 3)),
        Arguments.of(new DoubleDataType(), (double) 1),
        Arguments.of(new DoubleListDataType(), List.of((double) 1, (double) 2, (double) 3)),
        Arguments.of(new FloatDataType(), (float) 1),
        Arguments.of(new FloatListDataType(), List.of((float) 1, (float) 2, (float) 3)),
        Arguments.of(new IntegerDataType(), 1),
        Arguments.of(new IntegerListDataType(), List.of(1, 2, 3)),
        Arguments.of(new LongDataType(), (long) 1),
        Arguments.of(new LongListDataType(), List.of((long) 1, (long) 2, (long) 3)),
        Arguments.of(new ShortDataType(), (short) 1),
        Arguments.of(new ShortListDataType(), List.of((short) 1, (short) 2, (short) 3)),
        Arguments.of(new ListDataType<>(new IntegerDataType()), List.of(1, 2, 3)),
        Arguments.of(new CoordinateDataType(), new Coordinate(-180, -90)),
        Arguments.of(new CoordinateDataType(), new Coordinate(180, 90)),
        Arguments.of(new LonLatDataType(), new Coordinate(-180, -90)),
        Arguments.of(new LonLatDataType(), new Coordinate(180, 90)),
        Arguments.of(new SmallIntegerDataType(1), (int) Math.pow(2, 7) - 1),
        Arguments.of(new SmallIntegerDataType(1), (int) -Math.pow(2, 7)),
        Arguments.of(new SmallIntegerDataType(2), (int) Math.pow(2, 15) - 1),
        Arguments.of(new SmallIntegerDataType(2), (int) -Math.pow(2, 15)),
        Arguments.of(new SmallIntegerDataType(3), (int) Math.pow(2, 23) - 1),
        Arguments.of(new SmallIntegerDataType(3), (int) -Math.pow(2, 23)),
        Arguments.of(new SmallIntegerDataType(4), Integer.MAX_VALUE),
        Arguments.of(new SmallIntegerDataType(4), Integer.MIN_VALUE),
        Arguments.of(new SmallLongDataType(1), (long) Math.pow(2, 7) - 1),
        Arguments.of(new SmallLongDataType(1), (long) -Math.pow(2, 7)),
        Arguments.of(new SmallLongDataType(2), (long) Math.pow(2, 15) - 1),
        Arguments.of(new SmallLongDataType(2), (long) -Math.pow(2, 15)),
        Arguments.of(new SmallLongDataType(3), (long) Math.pow(2, 23) - 1),
        Arguments.of(new SmallLongDataType(3), (long) -Math.pow(2, 23)),
        Arguments.of(new SmallLongDataType(4), (long) Integer.MAX_VALUE),
        Arguments.of(new SmallLongDataType(4), (long) Integer.MIN_VALUE),
        Arguments.of(new SmallLongDataType(5), (long) Math.pow(2, 39) - 1),
        Arguments.of(new SmallLongDataType(5), (long) -Math.pow(2, 39)),
        Arguments.of(new SmallLongDataType(6), (long) Math.pow(2, 47) - 1),
        Arguments.of(new SmallLongDataType(6), (long) -Math.pow(2, 47)),
        Arguments.of(new SmallLongDataType(7), (long) Math.pow(2, 55) - 1),
        Arguments.of(new SmallLongDataType(7), (long) -Math.pow(2, 55)),
        Arguments.of(new SmallLongDataType(8), Long.MAX_VALUE),
        Arguments.of(new SmallLongDataType(8), Long.MIN_VALUE),
        Arguments.of(new PairDataType<>(new LongDataType(), new LongDataType()),
            new PairDataType.Pair<>(1l, 2l)));
  }
}
