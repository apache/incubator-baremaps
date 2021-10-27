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

package com.baremaps.osm.cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LongListMapper implements CacheMapper<List<Long>> {

  @Override
  public int size(List<Long> value) {
    return 4 + 8 * value.size();
  }

  @Override
  public List<Long> read(ByteBuffer buffer) {
    if (buffer == null) {
      return null;
    }
    int size = buffer.getInt();
    List<Long> values = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      values.add(buffer.getLong());
    }
    buffer.flip();
    return values;
  }

  @Override
  public void write(ByteBuffer buffer, List<Long> value) {
    buffer.putInt(value.size());
    for (Long v : value) {
      buffer.putLong(v);
    }
    buffer.flip();
  }
}
