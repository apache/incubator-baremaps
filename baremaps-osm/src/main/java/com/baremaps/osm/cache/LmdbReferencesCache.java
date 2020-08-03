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
import javax.inject.Inject;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

public class LmdbReferencesCache extends LmdbCache<Long, List<Long>> {

  @Inject
  public LmdbReferencesCache(Env<ByteBuffer> env) {
    super(env, env.openDbi("references", DbiFlags.MDB_CREATE));
  }

  @Override
  public ByteBuffer buffer(Long key) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(20);
    buffer.put(String.format("%020d", key).getBytes()).flip();
    return buffer.putLong(key);
  }

  public List<Long> read(ByteBuffer buffer) {
    if (buffer == null) {
      return null;
    }
    int size = buffer.getInt();
    List<Long> values = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      values.add(buffer.getLong());
    }
    return values;
  }

  public ByteBuffer write(List<Long> value) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(4 + 8 * value.size());
    buffer.putInt(value.size());
    for (Long v : value) {
      buffer.putLong(v);
    }
    buffer.flip();
    return buffer;
  }

}
