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

package org.apache.baremaps.flatgeobuf;

import java.io.IOException;
import java.io.InputStream;

public class BoundedInputStream extends InputStream {
  private final InputStream in;
  private long remaining;

  public BoundedInputStream(InputStream in, long size) {
    this.in = in;
    this.remaining = size;
  }

  @Override
  public int read() throws IOException {
    if (remaining == 0) {
      return -1;
    }
    int result = in.read();
    if (result != -1) {
      remaining--;
    }
    return result;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (remaining == 0) {
      return -1;
    }
    int toRead = (int) Math.min(len, remaining);
    int result = in.read(b, off, toRead);
    if (result != -1) {
      remaining -= result;
    }
    return result;
  }

  @Override
  public long skip(long n) throws IOException {
    long toSkip = Math.min(n, remaining);
    long skipped = in.skip(toSkip);
    remaining -= skipped;
    return skipped;
  }

  @Override
  public int available() throws IOException {
    return (int) Math.min(in.available(), remaining);
  }

  @Override
  public void close() throws IOException {
    in.close();
  }
}
