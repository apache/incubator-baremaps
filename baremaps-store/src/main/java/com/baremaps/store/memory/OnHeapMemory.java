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

public class OnHeapMemory implements Memory {

  private final int segmentBytes;

  private final int segmentBits;

  private final long segmentMask;

  private final List<ByteBuffer> segments = new ArrayList<>();

  public OnHeapMemory() {
    this(1 << 20);
  }

  public OnHeapMemory(int segmentBytes) {
    if ((segmentBytes & -segmentBytes) != segmentBytes) {
      throw new IllegalArgumentException("The segment size must be a power of 2");
    }
    this.segmentBytes = segmentBytes;
    this.segmentBits = (int) (Math.log(segmentBytes) / Math.log(2));
    this.segmentMask = (1L << segmentBits) - 1;
  }

  @Override
  public int segmentBytes() {
    return segmentBytes;
  }

  @Override
  public long segmentBits() {
    return segmentBits;
  }

  @Override
  public long segmentMask() {
    return segmentMask;
  }

  @Override
  public ByteBuffer segment(int index) {
    while (segments.size() <= index) {
      segments.add(null);
    }
    ByteBuffer segment = segments.get(index);
    if (segment == null) {
      segment = newSegment(index);
    }
    return segment;
  }

  private synchronized ByteBuffer newSegment(int index) {
    ByteBuffer segment = segments.get(index);
    if (segment == null) {
      segment = ByteBuffer.allocate(segmentBytes);
      segments.set(index, segment);
    }
    return segment;
  }

  @Override
  public void close() throws Exception {}
}
