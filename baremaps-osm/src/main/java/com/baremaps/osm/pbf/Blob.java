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
import com.baremaps.osm.binary.Fileformat.BlobHeader;
import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.stream.StreamException;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Blob {

  private final BlobHeader header;
  private final byte[] data;

  public Blob(BlobHeader header, byte[] data) {
    this.header = header;
    this.data = data;
  }


  public ByteString inflateData() throws DataFormatException, InvalidProtocolBufferException {
    Fileformat.Blob blob = Fileformat.Blob.parseFrom(data);
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

  public FileBlock readFileBlock() {
    try {
      ByteString data = inflateData();
      switch (this.header.getType()) {
        case "OSMHeader":
          Osmformat.HeaderBlock headerBlock = Osmformat.HeaderBlock.parseFrom(data);
          return new HeaderBlock(headerBlock);
        case "OSMData":
          Osmformat.PrimitiveBlock dataBlock = Osmformat.PrimitiveBlock.parseFrom(data);
          return new DataBlock(dataBlock);
        default:
          throw new UnsupportedOperationException("Unknown file block: " + this.header.getType());
      }
    } catch (InvalidProtocolBufferException | DataFormatException e) {
      throw new StreamException(e);
    }
  }

  public long size() {
    return 8 + header.getDatasize() + data.length;
  }

}
