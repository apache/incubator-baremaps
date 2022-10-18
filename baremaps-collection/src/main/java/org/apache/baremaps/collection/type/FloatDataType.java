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

/** A {@link DataType} for reading and writing floats in {@link ByteBuffer}s. */
public class FloatDataType implements SizedDataType<Float> {

  /** {@inheritDoc} */
  @Override
  public int size(Float value) {
    return 4;
  }

  /** {@inheritDoc} */
  @Override
  public void write(ByteBuffer buffer, int position, Float value) {
    buffer.putFloat(position, value);
  }

  /** {@inheritDoc} */
  @Override
  public Float read(ByteBuffer buffer, int position) {
    return buffer.getFloat(position);
  }
}
