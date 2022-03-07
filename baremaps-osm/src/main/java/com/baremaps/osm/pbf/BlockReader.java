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

/** A reader that extracts blocks from OpenStreetMap blobs. */
public class BlockReader {

  private final Blob blob;

  public BlockReader(Blob blob) {
    this.blob = blob;
  }

  /**
   * Reads the provided {@code Blob} and returns the corresponding {@code Block}.
   *
   * @param blob the blob
   * @return the block
   */
  public static Block read(Blob blob) {
    return new BlockReader(blob).read();
  }

  public Block read() {
    switch (blob.header().getType()) {
      case "OSMHeader":
        return HeaderBlockReader.read(blob);
      case "OSMData":
        return DataBlockReader.read(blob);
      default:
        throw new RuntimeException("Unknown blob type");
    }
  }
}
