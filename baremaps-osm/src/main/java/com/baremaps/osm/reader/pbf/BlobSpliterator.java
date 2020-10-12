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

package com.baremaps.osm.reader.pbf;

import com.baremaps.osm.binary.Fileformat;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.function.Consumer;

public class BlobSpliterator implements Spliterator<Blob> {

  protected final DataInputStream input;

  public BlobSpliterator(InputStream input) {
    this.input = new DataInputStream(input);
  }

  @Override
  public Spliterator<Blob> trySplit() {
    return null;
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return IMMUTABLE;
  }

  @Override
  public boolean tryAdvance(Consumer<? super Blob> action) {
    try {
      // Read blob header
      int headerSize = input.readInt();
      byte[] headerBytes = new byte[headerSize];
      input.readFully(headerBytes);
      Fileformat.BlobHeader header = Fileformat.BlobHeader.parseFrom(headerBytes);

      // Read blob data
      int dataSize = header.getDatasize();
      byte[] data = new byte[dataSize];
      input.readFully(data);

      action.accept(new Blob(header, data,8 + headerSize + dataSize));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
