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



import java.nio.ByteBuffer;
import org.apache.baremaps.utils.GeometryUtils;
import org.locationtech.jts.geom.Geometry;

/** A {@link DataType} for reading and writing {@link Geometry} in {@link ByteBuffer}s. */
public class WKBDataType implements DataType<Geometry> {

  /** {@inheritDoc} */
  @Override
  public int size(Geometry value) {
    byte[] bytes = GeometryUtils.serialize(value);
    return Integer.BYTES + bytes.length;
  }

  /** {@inheritDoc} */
  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, Geometry value) {
    byte[] bytes = GeometryUtils.serialize(value);
    buffer.putInt(position, Integer.BYTES + bytes.length);
    buffer.put(position + Integer.BYTES, bytes);
  }

  /** {@inheritDoc} */
  @Override
  public Geometry read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    byte[] bytes = new byte[Math.max(size - Integer.BYTES, 0)];
    buffer.get(position + Integer.BYTES, bytes);
    return GeometryUtils.deserialize(bytes);
  }
}
