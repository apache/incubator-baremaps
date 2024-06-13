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

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.math.LongMath;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class PMTilesUtilsTest {

  @Test
  void decodeVarInt() throws IOException {
    var b = new LittleEndianDataInputStream(new ByteArrayInputStream(new byte[] {
        (byte) 0, (byte) 1,
        (byte) 127, (byte) 0xe5,
        (byte) 0x8e, (byte) 0x26
    }));
    assertEquals(0, PMTilesUtils.readVarInt(b));
    assertEquals(1, PMTilesUtils.readVarInt(b));
    assertEquals(127, PMTilesUtils.readVarInt(b));
    assertEquals(624485, PMTilesUtils.readVarInt(b));
    b = new LittleEndianDataInputStream(new ByteArrayInputStream(new byte[] {
        (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0x0f,
    }));
    assertEquals(9007199254740991L, PMTilesUtils.readVarInt(b));
  }

  @Test
  void encodeVarInt() throws IOException {
    for (long i = 0; i < 1000; i++) {
      var array = new ByteArrayOutputStream();
      PMTilesUtils.writeVarInt(array, i);
      var input = new LittleEndianDataInputStream(new ByteArrayInputStream(array.toByteArray()));
      assertEquals(i, PMTilesUtils.readVarInt(input));
    }
    for (long i = Long.MAX_VALUE - 1000; i < Long.MAX_VALUE; i++) {
      var array = new ByteArrayOutputStream();
      PMTilesUtils.writeVarInt(array, i);
      var input = new LittleEndianDataInputStream(new ByteArrayInputStream(array.toByteArray()));
      assertEquals(i, PMTilesUtils.readVarInt(input));
    }
  }

  @Test
  void zxyToTileId() {
    assertEquals(0, PMTilesUtils.zxyToTileId(0, 0, 0));
    assertEquals(1, PMTilesUtils.zxyToTileId(1, 0, 0));
    assertEquals(2, PMTilesUtils.zxyToTileId(1, 0, 1));
    assertEquals(3, PMTilesUtils.zxyToTileId(1, 1, 1));
    assertEquals(4, PMTilesUtils.zxyToTileId(1, 1, 0));
    assertEquals(5, PMTilesUtils.zxyToTileId(2, 0, 0));
  }

  @Test
  void tileIdToZxy() {
    assertArrayEquals(new long[] {0, 0, 0}, PMTilesUtils.tileIdToZxy(0));
    assertArrayEquals(new long[] {1, 0, 0}, PMTilesUtils.tileIdToZxy(1));
    assertArrayEquals(new long[] {1, 0, 1}, PMTilesUtils.tileIdToZxy(2));
    assertArrayEquals(new long[] {1, 1, 1}, PMTilesUtils.tileIdToZxy(3));
    assertArrayEquals(new long[] {1, 1, 0}, PMTilesUtils.tileIdToZxy(4));
    assertArrayEquals(new long[] {2, 0, 0}, PMTilesUtils.tileIdToZxy(5));
  }

  @Test
  void aLotOfTiles() {
    for (int z = 0; z < 9; z++) {
      for (long x = 0; x < 1 << z; x++) {
        for (long y = 0; y < 1 << z; y++) {
          var result = PMTilesUtils.tileIdToZxy(PMTilesUtils.zxyToTileId(z, x, y));
          if (result[0] != z || result[1] != x || result[2] != y) {
            fail("roundtrip failed");
          }
        }
      }
    }
  }

  @Test
  void tileExtremes() {
    for (var z = 0; z < 27; z++) {
      var dim = LongMath.pow(2, z) - 1;
      var tl = PMTilesUtils.tileIdToZxy(PMTilesUtils.zxyToTileId(z, 0, 0));
      assertArrayEquals(new long[] {z, 0, 0}, tl);
      var tr = PMTilesUtils.tileIdToZxy(PMTilesUtils.zxyToTileId(z, dim, 0));
      assertArrayEquals(new long[] {z, dim, 0}, tr);
      var bl = PMTilesUtils.tileIdToZxy(PMTilesUtils.zxyToTileId(z, 0, dim));
      assertArrayEquals(new long[] {z, 0, dim}, bl);
      var br = PMTilesUtils.tileIdToZxy(PMTilesUtils.zxyToTileId(z, dim, dim));
      assertArrayEquals(new long[] {z, dim, dim}, br);
    }
  }

  @Test
  void invalidTiles() {
    assertThrows(RuntimeException.class, () -> PMTilesUtils.tileIdToZxy(9007199254740991L));
    assertThrows(RuntimeException.class, () -> PMTilesUtils.zxyToTileId(27, 0, 0));
    assertThrows(RuntimeException.class, () -> PMTilesUtils.zxyToTileId(0, 1, 1));
  }

  @Test
  void decodeHeader() throws IOException {
    var file = TestFiles.resolve("baremaps-testing/data/pmtiles/test_fixture_1.pmtiles");
    try (var channel = FileChannel.open(file)) {
      var input = new LittleEndianDataInputStream(Channels.newInputStream(channel));
      var header = PMTilesUtils.deserializeHeader(input);
      assertEquals(127, header.getRootDirectoryOffset());
      assertEquals(25, header.getRootDirectoryLength());
      assertEquals(152, header.getJsonMetadataOffset());
      assertEquals(247, header.getJsonMetadataLength());
      assertEquals(0, header.getLeafDirectoryOffset());
      assertEquals(0, header.getLeafDirectoryLength());
      assertEquals(399, header.getTileDataOffset());
      assertEquals(69, header.getTileDataLength());
      assertEquals(1, header.getNumAddressedTiles());
      assertEquals(1, header.getNumTileEntries());
      assertEquals(1, header.getNumTileContents());
      assertFalse(header.isClustered());
      assertEquals(Compression.GZIP, header.getInternalCompression());
      assertEquals(Compression.GZIP, header.getTileCompression());
      assertEquals(TileType.MVT, header.getTileType());
      assertEquals(0, header.getMinZoom());
      assertEquals(0, header.getMaxZoom());
      assertEquals(0, header.getMinLon());
      assertEquals(0, header.getMinLat());
      assertEquals(1, Math.round(header.getMaxLon()));
      assertEquals(1, Math.round(header.getMaxLat()));
    }
  }

  @Test
  void encodeHeader() throws IOException {
    var header = new Header(
        127,
        25,
        152,
        247,
        0,
        0,
        399,
        69,
        1,
        1,
        1,
        10,
        false,
        Compression.GZIP,
        Compression.GZIP,
        TileType.MVT,
        0,
        0,
        0,
        1,
        1,
        0,
        0,
        0,
        0);

    var array = new ByteArrayOutputStream();
    array.write(PMTilesUtils.serializeHeader(header));

    var input = new LittleEndianDataInputStream(new ByteArrayInputStream(array.toByteArray()));
    var header2 = PMTilesUtils.deserializeHeader(input);

    assertEquals(header, header2);
  }

  @Test
  void searchForMissingEntry() {
    var entries = new ArrayList<Entry>();
    assertNull(PMTilesUtils.findTile(entries, 101));
  }

  @Test
  void searchForFirstEntry() {
    var entry = new Entry(100, 1, 1, 1);
    var entries = new ArrayList<Entry>();
    entries.add(entry);
    assertEquals(entry, PMTilesUtils.findTile(entries, 100));
  }

  @Test
  void searchWithRunLength() {
    var entry = new Entry(3, 3, 1, 2);
    var entries = new ArrayList<Entry>();
    entries.add(entry);
    entries.add(new Entry(5, 5, 1, 2));
    assertEquals(entry, PMTilesUtils.findTile(entries, 4));
  }

  @Test
  void searchWithMultipleTileEntries() {
    var entries = new ArrayList<Entry>();
    entries.add(new Entry(100, 1, 1, 2));
    var entry = PMTilesUtils.findTile(entries, 101);
    assertEquals(1, entry.getOffset());
    assertEquals(1, entry.getLength());

    entries = new ArrayList<Entry>();
    entries.add(new Entry(100, 1, 1, 1));
    entries.add(new Entry(150, 2, 2, 2));
    entry = PMTilesUtils.findTile(entries, 151);
    assertEquals(2, entry.getOffset());
    assertEquals(2, entry.getLength());

    entries = new ArrayList<>();
    entries.add(new Entry(50, 1, 1, 2));
    entries.add(new Entry(100, 2, 2, 1));
    entries.add(new Entry(150, 3, 3, 1));
    entry = PMTilesUtils.findTile(entries, 51);
    assertEquals(1, entry.getOffset());
    assertEquals(1, entry.getLength());
  }

  @Test
  void leafSearch() {
    var entries = new ArrayList<Entry>();
    entries.add(new Entry(100, 1, 1, 0));
    var entry = PMTilesUtils.findTile(entries, 150);
    assertEquals(1, entry.getOffset());
    assertEquals(1, entry.getLength());
  }

  @Test
  void buildRootLeaves() throws IOException {
    var entries = List.of(new Entry(100, 1, 1, 0));
    var directories = PMTilesUtils.buildRootLeaves(entries, 1, Compression.NONE);
    assertEquals(1, directories.getNumLeaves());

  }

  @Test
  void optimizeDirectories() throws IOException {
    var random = new Random(3857);
    var entries = new ArrayList<Entry>();
    entries.add(new Entry(0, 0, 100, 1));
    var directories = PMTilesUtils.optimizeDirectories(entries, 100, Compression.NONE);
    assertFalse(directories.getLeaves().length > 0);
    assertEquals(0, directories.getNumLeaves());

    entries = new ArrayList<>();
    int offset = 0;
    for (var i = 0; i < 1000; i++) {
      var randTileSize = random.nextInt(1000000);
      entries.add(new Entry(i, offset, randTileSize, 1));
      offset += randTileSize;
    }
    directories = PMTilesUtils.optimizeDirectories(entries, 1024, Compression.NONE);
    assertFalse(directories.getRoot().length > 1024);
    assertNotEquals(0, directories.getNumLeaves());
    assertNotEquals(0, directories.getLeaves().length);
  }
}
