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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LongListDataType implements DataType<List<Long>> {

  @Override
  public int size(List<Long> values) {
    return 4 + values.size() * 8;
  }

  @Override
  public void write(ByteBuffer buffer, int position, List<Long> values) {
    buffer.putInt(position, values.size());
    position += 4;
    for (Long value : values) {
      buffer.putLong(position, value);
      position += 8;
    }
  }

  @Override
  public List<Long> read(ByteBuffer buffer, int position) {
    int size = buffer.getInt(position);
    position += 4;
    List<Long> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(buffer.getLong(position));
      position += 8;
    }
    return list;
  }
}
