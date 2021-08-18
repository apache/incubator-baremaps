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

public class BlockReader {

  private final Blob blob;

  public BlockReader(Blob blob) {
    this.blob = blob;
  }

  public Block readBlock() {
    switch (blob.header().getType()) {
      case "OSMHeader":
        return BlobUtils.readHeaderBlock(blob);
      case "OSMData":
        return BlobUtils.readDataBlock(blob);
      default:
        throw new RuntimeException("Unknown blob type");
    }
  }
}
