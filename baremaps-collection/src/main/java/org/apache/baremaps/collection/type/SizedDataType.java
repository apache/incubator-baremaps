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

/**
 * A {@link DataType} for reading and writing values in {@link ByteBuffer}s whose size is fixed.
 *
 * @param <T>
 */
public interface SizedDataType<T> extends DataType<T> {

  /**
   * Returns the size of the data type.
   *
   * @return the size of the data type.
   */
  default int size() {
    return size(null);
  }
}
