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

package org.apache.baremaps.data.memory;

import java.io.IOException;
import java.nio.ByteBuffer;

/** Memory implementation that uses on-heap byte buffers for storage. */
public class OnHeapMemory extends Memory<ByteBuffer> {

  /** Constructs an OnHeapMemory with a 1MB default segment size. */
  public OnHeapMemory() {
    this(1 << 20);
  }

  /**
   * Constructs an OnHeapMemory with the specified segment size.
   *
   * @param segmentSize the size of each segment in bytes
   */
  public OnHeapMemory(int segmentSize) {
    super(segmentSize);
  }

  /** {@inheritDoc} */
  @Override
  protected ByteBuffer allocate(int index, int size) {
    return ByteBuffer.allocate(size);
  }

  /**
   * On-heap buffers don't require explicit cleanup.
   * 
   * {@inheritDoc}
   */
  @Override
  public synchronized void close() throws IOException {
    // On-heap ByteBuffers don't need special cleanup
  }

  /**
   * Clears all segments to allow garbage collection.
   * 
   * {@inheritDoc}
   */
  @Override
  public synchronized void clear() throws IOException {
    // Clear the segment list to allow GC to reclaim the memory
    segments.clear();
  }
}
