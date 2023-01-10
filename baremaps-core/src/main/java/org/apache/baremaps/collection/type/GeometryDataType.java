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
import org.apache.baremaps.openstreetmap.utils.GeometryUtils;
import org.locationtech.jts.geom.Geometry;

public class GeometryDataType implements DataType<Geometry> {

  @Override
  public int size(Geometry value) {
    return Integer.BYTES + GeometryUtils.serialize(value).length;
  }

  @Override
  public int size(ByteBuffer buffer, int position) {
    return buffer.getInt(position);
  }

  @Override
  public void write(ByteBuffer buffer, int position, Geometry value) {
    byte[] bytes = GeometryUtils.serialize(value);
    buffer.putInt(position, Integer.BYTES + bytes.length);
    buffer.put(position + Integer.BYTES, bytes);
  }

  @Override
  public Geometry read(ByteBuffer buffer, int position) {
    int size = size(buffer, position);
    byte[] bytes = new byte[size - Integer.BYTES];
    buffer.get(position + Integer.BYTES, bytes);
    return GeometryUtils.deserialize(bytes);
  }
}
