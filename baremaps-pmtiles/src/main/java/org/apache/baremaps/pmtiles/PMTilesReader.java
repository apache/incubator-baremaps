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

package org.apache.baremaps.pmtiles;

import com.google.common.io.LittleEndianDataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PMTilesReader {

  private final Path path;

  private Header header;

  private List<Entry> rootEntries;

  public PMTilesReader(Path path) {
    this.path = path;
  }

  public Header getHeader() {
    if (header == null) {
      try (var inputStream = new LittleEndianDataInputStream(Files.newInputStream(path))) {
        header = PMTiles.deserializeHeader(inputStream);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return header;
  }

  public List<Entry> getRootDirectory() {
    if (rootEntries == null) {
      var header = getHeader();
      rootEntries = getDirectory(header.getRootDirectoryOffset());
    }
    return rootEntries;
  }

  public List<Entry> getDirectory(long offset) {
    var header = getHeader();
    try (var input = Files.newInputStream(path)) {
      long skipped = 0;
      while (skipped < offset) {
        skipped += input.skip(offset - skipped);
      }
      try (var decompressed =
          new LittleEndianDataInputStream(header.getInternalCompression().decompress(input))) {
        return PMTiles.deserializeEntries(decompressed);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public ByteBuffer getTile(int z, long x, long y) {
    var tileId = PMTiles.zxyToTileId(z, x, y);
    var header = getHeader();
    var entries = getRootDirectory();
    var entry = PMTiles.findTile(entries, tileId);

    if (entry == null) {
      return null;
    }

    try (var channel = FileChannel.open(path)) {
      var compressed = ByteBuffer.allocate((int) entry.getLength());
      channel.position(header.getTileDataOffset() + entry.getOffset());
      channel.read(compressed);
      compressed.flip();
      try (var tile = new ByteArrayInputStream(compressed.array())) {
        return ByteBuffer.wrap(tile.readAllBytes());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
