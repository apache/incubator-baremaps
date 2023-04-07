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

package org.apache.baremaps.collection.type.geometry;

import java.nio.ByteBuffer;
import org.apache.baremaps.collection.type.DataType;
import org.locationtech.jts.geom.Coordinate;

/**
 * A data type for {@link Coordinate} arrays.
 */
public class CoordinateArrayDataType implements DataType<Coordinate[]> {

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(Coordinate[] value) {
    return Integer.BYTES + Double.BYTES * 2 * value.length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(ByteBuffer buffer, int position, Coordinate[] value) {
    buffer.putInt(position, size(value));
    position += Integer.BYTES;
    for (int i = 0; i < value.length; i++) {
      Coordinate coordinate = value[i];
      buffer.putDouble(position, coordinate.x);
      position += Double.BYTES;
      buffer.putDouble(position, coordinate.y);
      position += Double.BYTES;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Coordinate[] read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    int numPoints = (size - Integer.BYTES) / (Double.BYTES * 2);
    position += Integer.BYTES;
    Coordinate[] coordinates = new Coordinate[numPoints];
    for (int i = 0; i < numPoints; i++) {
      double x = buffer.getDouble(position);
      double y = buffer.getDouble(position + Double.BYTES);
      coordinates[i] = new Coordinate(x, y);
      position += Double.BYTES * 2;
    }
    return coordinates;
  }
}
