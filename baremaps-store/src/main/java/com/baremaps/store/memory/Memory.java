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

public abstract class Memory {

  private final int segmentSize;

  private final long segmentShift;

  private final long segmentMask;

  public Memory(int segmentSize) {
    if ((segmentSize & -segmentSize) != segmentSize) {
      throw new IllegalArgumentException("The segment size must be a power of 2");
    }
    this.segmentSize = segmentSize;
    this.segmentShift = (int) (Math.log(this.segmentSize) / Math.log(2));
    this.segmentMask = this.segmentSize - 1;
  }

  public int segmentSize() {
    return segmentSize;
  }

  public long segmentShift() {
    return segmentShift;
  }

  public long segmentMask() {
    return segmentMask;
  }

  public abstract ByteBuffer segment(int index);
}
