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

package com.baremaps.osm.lmdb;

import com.baremaps.osm.cache.ReferenceCache;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

/** A {@code Cache} for references baked by LMDB. */
public class LmdbReferencesCache extends LmdbCache<Long, List<Long>> implements ReferenceCache {

  /** Constructs a {@code LmdbReferencesCache}. */
  public LmdbReferencesCache(Env<ByteBuffer> env) {
    super(env, env.openDbi("references", DbiFlags.MDB_CREATE));
  }

  @Override
  protected ByteBuffer buffer(Long key) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(20);
    return buffer.putLong(key);
  }

  @Override
  protected List<Long> read(ByteBuffer buffer) {
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

  @Override
  protected ByteBuffer write(List<Long> value) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(4 + 8 * value.size());
    buffer.putInt(value.size());
    for (Long v : value) {
      buffer.putLong(v);
    }
    buffer.flip();
    return buffer;
  }
}
