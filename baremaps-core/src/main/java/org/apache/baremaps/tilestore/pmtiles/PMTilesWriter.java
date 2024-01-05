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

package org.apache.baremaps.tilestore.pmtiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PMTilesWriter {

  private Compression compression = Compression.Gzip;

  private Path path;

  private Map<String, Object> metadata = new HashMap<>();

  private List<Entry> entries;

  private Map<Long, Long> tileHashToOffset;

  private Long lastTileHash = null;

  private Path tilePath;

  private boolean clustered = true;

  private int minZoom = 0;

  private int maxZoom = 14;

  private double minLon = -180;

  private double minLat = -90;

  private double maxLon = 180;

  private double maxLat = 90;

  private int centerZoom = 3;

  private double centerLat = 0;

  private double centerLon = 0;

  public PMTilesWriter(Path path) throws IOException {
    this(path, new ArrayList<>(), new HashMap<>());
  }

  public PMTilesWriter(Path path, List<Entry> entries, Map<Long, Long> tileHashToOffset)
      throws IOException {
    this.path = path;
    this.entries = entries;
    this.tileHashToOffset = tileHashToOffset;
    this.tilePath = Files.createTempFile(path.getParent(), "tiles_", ".tmp");
  }

  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public void setTile(int z, int x, int y, byte[] bytes) throws IOException {
    // Write the tile
    var tileId = PMTiles.zxyToTileId(z, x, y);
    var tileLength = bytes.length;
    Long tileHash = Hashing.farmHashFingerprint64().hashBytes(bytes).asLong();

    // If the tile is not greater than the last one, the index is not clustered
    if (entries.size() > 0 && tileId < entries.get(entries.size() - 1).getTileId()) {
      clustered = false;
    }

    // If the tile is the same as the last one, increment the run length
    if (clustered && tileHash.equals(lastTileHash)) {
      var lastEntry = entries.get(entries.size() - 1);
      lastEntry.setRunLength(lastEntry.getRunLength() + 1);
    }

    // Else, if the tile is the same as the last one, increment the run length
    else if (tileHashToOffset.containsKey(tileHash)) {
      var tileOffset = tileHashToOffset.get(tileHash);
      entries.add(new Entry(tileId, tileOffset, tileLength, 1));
    }

    // Else, write the tile and add it to the index
    else {
      var tileOffset = Files.size(tilePath);
      tileHashToOffset.put(tileHash, tileOffset);
      lastTileHash = tileHash;
      try (var output = new FileOutputStream(tilePath.toFile(), true)) {
        output.write(bytes);
        entries.add(new Entry(tileId, tileOffset, tileLength, 1));
      }
    }
  }

  public void setMinZoom(int minZoom) {
    this.minZoom = minZoom;
  }

  public void setMaxZoom(int maxZoom) {
    this.maxZoom = maxZoom;
  }

  public void setMinLon(double minLon) {
    this.minLon = minLon;
  }

  public void setMinLat(double minLat) {
    this.minLat = minLat;
  }

  public void setMaxLon(double maxLon) {
    this.maxLon = maxLon;
  }

  public void setMaxLat(double maxLat) {
    this.maxLat = maxLat;
  }

  public void setCenterZoom(int centerZoom) {
    this.centerZoom = centerZoom;
  }

  public void setCenterLat(double centerLat) {
    this.centerLat = centerLat;
  }

  public void setCenterLon(double centerLon) {
    this.centerLon = centerLon;
  }

  public void write() throws IOException {
    // Sort the entries by tile id
    if (!clustered) {
      entries.sort(Comparator.comparingLong(Entry::getTileId));
    }

    var directories = PMTiles.optimizeDirectories(entries, 16247, compression);

    byte[] metadataBytes;
    try (var metadataOutput = new ByteArrayOutputStream()) {
      try (var compressedMetadataOutput = compression.compress(metadataOutput)) {
        new ObjectMapper().writeValue(compressedMetadataOutput, metadata);
      }
      metadataBytes = metadataOutput.toByteArray();
    }

    var rootOffset = 127;
    var rootLength = directories.getRoot().length;
    var metadataOffset = rootOffset + rootLength;
    var metadataLength = metadataBytes.length;
    var leavesOffset = metadataOffset + metadataLength;
    var leavesLength = directories.getLeaves().length;
    var tilesOffset = leavesOffset + leavesLength;
    var tilesLength = Files.size(tilePath);
    var numTiles = entries.size();

    var header = new Header();
    header.setNumAddressedTiles(numTiles);
    header.setNumTileEntries(numTiles);
    header.setNumTileContents(tileHashToOffset.size());
    header.setClustered(true);

    header.setInternalCompression(compression);
    header.setTileCompression(compression);
    header.setTileType(TileType.mvt);
    header.setRootOffset(rootOffset);
    header.setRootLength(rootLength);
    header.setMetadataOffset(metadataOffset);
    header.setMetadataLength(metadataLength);
    header.setLeavesOffset(leavesOffset);
    header.setLeavesLength(leavesLength);
    header.setTilesOffset(tilesOffset);
    header.setTilesLength(tilesLength);

    header.setMinZoom(minZoom);
    header.setMaxZoom(maxZoom);
    header.setMinLon(minLon);
    header.setMinLat(minLat);
    header.setMaxLon(maxLon);
    header.setMaxLat(maxLat);
    header.setCenterZoom(centerZoom);
    header.setCenterLat(centerLat);
    header.setCenterLon(centerLon);

    try (var output = new FileOutputStream(path.toFile())) {
      output.write(PMTiles.serializeHeader(header));
      output.write(directories.getRoot());
      output.write(metadataBytes);
      output.write(directories.getLeaves());
      Files.copy(tilePath, output);
    } finally {
      Files.delete(tilePath);
    }
  }

}
