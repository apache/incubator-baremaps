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

import com.baremaps.osm.domain.Blob;
import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.domain.HeaderBlock;
import com.baremaps.stream.StreamException;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.zip.DataFormatException;

/** Utility methods for reading OpenStreetMap blocks. */
public class BlobUtils {

  private BlobUtils() {}

  /**
   * Reads the provided {@code Blob} and returns the corresponding {@code Block}.
   *
   * @param blob the blob
   * @return the block
   */
  public static Block readBlock(Blob blob) {
    return new BlockReader(blob).readBlock();
  }

  /**
   * Reads the provided header {@code Blob} and returns the corresponding {@code HeaderBlock}.
   *
   * @param blob the header blob
   * @return the header block
   */
  public static HeaderBlock readHeaderBlock(Blob blob) {
    try {
      return new HeaderBlockReader(blob).readHeaderBlock();
    } catch (DataFormatException | InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }

  /**
   * Reads the provided data {@code Blob} and returns the corresponding {@code DataBlock}.
   *
   * @param blob the data blob
   * @return the data block
   */
  public static DataBlock readDataBlock(Blob blob) {
    try {
      return new DataBlockReader(blob).readDataBlock();
    } catch (DataFormatException | InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }
}
