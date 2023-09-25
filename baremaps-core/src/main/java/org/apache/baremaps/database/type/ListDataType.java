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

package org.apache.baremaps.database.type;



import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link DataType} for reading and writing lists of objects in {@link ByteBuffer}s.
 */
public class ListDataType<T> implements DataType<List<T>> {

  public final DataType<T> dataType;

  /**
   * Constructs a {@link ListDataType} with a data type.
   *
   * @param dataType the data type of the values
   */
  public ListDataType(final DataType<T> dataType) {
    this.dataType = dataType;
  }

  /** {@inheritDoc} */
  @Override
  public int size(final List<T> values) {
    int size = Integer.BYTES;
    for (T value : values) {
      size += dataType.size(value);
    }
    return size;
  }

  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(final ByteBuffer buffer, final int position, final List<T> values) {
    buffer.putInt(position, size(values));
    int p = position + Integer.BYTES;
    for (T value : values) {
      dataType.write(buffer, p, value);
      p += dataType.size(value);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<T> read(final ByteBuffer buffer, final int position) {
    int size = buffer.getInt(position);
    var list = new ArrayList<T>(size);
    for (var p = position + Integer.BYTES; p < position + size; p += dataType.size(buffer, p)) {
      list.add(dataType.read(buffer, p));
    }
    return list;
  }
}
