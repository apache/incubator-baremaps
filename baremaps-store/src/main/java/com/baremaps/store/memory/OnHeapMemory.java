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

package com.baremaps.store.memory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class OnHeapMemory extends Memory {

  private final List<ByteBuffer> segments = new ArrayList<>();

  public OnHeapMemory() {
    this(1 << 20);
  }

  public OnHeapMemory(int segmentSize) {
    super(segmentSize);
  }

  public ByteBuffer segment(int index) {
    if (segments.size() <= index) {
      return allocate(index);
    }
    ByteBuffer segment = segments.get(index);
    if (segment == null) {
      return allocate(index);
    }
    return segment;
  }

  private synchronized ByteBuffer allocate(int index) {
    while (segments.size() <= index) {
      segments.add(null);
    }
    ByteBuffer segment = segments.get(index);
    if (segment == null) {
      segment = ByteBuffer.allocate(segmentSize());
      segments.set(index, segment);
    }
    return segment;
  }
}
