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

package com.baremaps.osm.pbf;

import com.baremaps.osm.binary.Fileformat;
import com.baremaps.osm.binary.Fileformat.Blob;
import com.google.protobuf.ByteString;
import java.io.DataInputStream;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class FileBlockSpliterator implements Spliterator<FileBlock> {

  protected final DataInputStream input;

  public FileBlockSpliterator(DataInputStream input) {
    this.input = input;
  }

  @Override
  public Spliterator<FileBlock> trySplit() {
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
  public boolean tryAdvance(Consumer<? super FileBlock> action) {
    try {
      int headerSize = input.readInt();
      byte[] headerData = new byte[headerSize];
      input.readFully(headerData);
      Fileformat.BlobHeader header = Fileformat.BlobHeader.parseFrom(headerData);
      int blobSize = header.getDatasize();
      byte[] blobData = new byte[blobSize];
      input.readFully(blobData);
      Fileformat.Blob blob = Fileformat.Blob.parseFrom(blobData);
      action.accept(new FileBlock(FileBlock.Type.valueOf(header.getType()), header.getIndexdata(), data(blob)));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private ByteString data(Blob blob) throws DataFormatException {
    if (blob.hasRaw()) {
      return blob.getRaw();
    } else if (blob.hasZlibData()) {
      byte[] bytes = new byte[blob.getRawSize()];
      Inflater inflater = new Inflater();
      inflater.setInput(blob.getZlibData().toByteArray());
      inflater.inflate(bytes);
      inflater.end();
      return ByteString.copyFrom(bytes);
    } else {
      throw new DataFormatException("Unsupported toPrimitiveBlock format");
    }
  }

}
