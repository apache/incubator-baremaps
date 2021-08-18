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

package com.baremaps.osm.progress;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class InputStreamProgress extends FilterInputStream {

  private final Consumer<Long> listener;

  private long position = 0;

  public InputStreamProgress(InputStream inputStream, Consumer<Long> listener) {
    super(inputStream);
    this.listener = listener;
  }

  @Override
  public int read() throws IOException {
    int b = super.read();
    if (b > 0) {
      position++;
      listener.accept(position);
    }
    return b;
  }

  @Override
  public int read(byte[] b) throws IOException {
    int n = super.read(b);
    if (n > 0) {
      position += n;
      listener.accept(position);
    }
    return n;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int n = super.read(b, off, len);
    if (n != -1) {
      position += n;
      listener.accept(position);
    }
    return n;
  }

  @Override
  public long skip(long n) throws IOException {
    long s = super.skip(n);
    if (s != -1) {
      position += s;
      listener.accept(position);
    }
    return s;
  }
}
