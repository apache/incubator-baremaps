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

public class LongMapper implements CacheMapper<Long> {

  @Override
  public int size(Long value) {
    return Long.BYTES;
  }

  @Override
  public Long read(ByteBuffer buffer) {
    Long value = buffer.getLong();
    buffer.flip();
    return value;
  }

  @Override
  public void write(ByteBuffer buffer, Long value) {
    buffer.putLong(value).flip();
  }
}
