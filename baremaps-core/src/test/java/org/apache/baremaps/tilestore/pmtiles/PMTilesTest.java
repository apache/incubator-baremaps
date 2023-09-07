/*
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

package org.apache.baremaps.tilestore.pmtiles;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.math.LongMath;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.baremaps.testing.TestFiles;
import org.apache.baremaps.vectortile.VectorTileDecoder;
import org.apache.baremaps.vectortile.VectorTileViewer;
import org.apache.baremaps.vectortile.VectorTileViewer.TilePanel;
import org.junit.jupiter.api.Test;

import javax.swing.*;

class PMTilesTest {

  @Test
  void decodeVarInt() {
    var b = ByteBuffer.wrap(new byte[] {
        (byte) 0, (byte) 1,
        (byte) 127, (byte) 0xe5,
        (byte) 0x8e, (byte) 0x26
    });
    assertEquals(PMTiles.decodeVarInt(b), 0);
    assertEquals(PMTiles.decodeVarInt(b), 1);
    assertEquals(PMTiles.decodeVarInt(b), 127);
    assertEquals(PMTiles.decodeVarInt(b), 624485);
    b = ByteBuffer.wrap(new byte[] {
        (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0x0f,
    });
    assertEquals(PMTiles.decodeVarInt(b), 9007199254740991L);
  }

  @Test
  void encodeVarInt() {
    var buffer = ByteBuffer.allocate(10);
    for (long i = 0; i < 1000; i++) {
      PMTiles.encodeVarInt(buffer, i);
      buffer.flip();
      assertEquals(i, PMTiles.decodeVarInt(buffer));
      buffer.clear();
    }
    for (long i = Long.MAX_VALUE - 1000; i < Long.MAX_VALUE; i++) {
      PMTiles.encodeVarInt(buffer, i);
      buffer.flip();
      assertEquals(i, PMTiles.decodeVarInt(buffer));
      buffer.clear();
    }
  }

  @Test
  void zxyToTileId() {
    assertEquals(PMTiles.zxyToTileId(0, 0, 0), 0);
    assertEquals(PMTiles.zxyToTileId(1, 0, 0), 1);
    assertEquals(PMTiles.zxyToTileId(1, 0, 1), 2);
    assertEquals(PMTiles.zxyToTileId(1, 1, 1), 3);
    assertEquals(PMTiles.zxyToTileId(1, 1, 0), 4);
    assertEquals(PMTiles.zxyToTileId(2, 0, 0), 5);
  }

  @Test
  void tileIdToZxy() {
    assertArrayEquals(PMTiles.tileIdToZxy(0), new long[] {0, 0, 0});
    assertArrayEquals(PMTiles.tileIdToZxy(1), new long[] {1, 0, 0});
    assertArrayEquals(PMTiles.tileIdToZxy(2), new long[] {1, 0, 1});
    assertArrayEquals(PMTiles.tileIdToZxy(3), new long[] {1, 1, 1});
    assertArrayEquals(PMTiles.tileIdToZxy(4), new long[] {1, 1, 0});
    assertArrayEquals(PMTiles.tileIdToZxy(5), new long[] {2, 0, 0});
  }

  @Test
  void aLotOfTiles() {
    for (int z = 0; z < 9; z++) {
      for (long x = 0; x < 1 << z; x++) {
        for (long y = 0; y < 1 << z; y++) {
          var result = PMTiles.tileIdToZxy(PMTiles.zxyToTileId(z, x, y));
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
      var tl = PMTiles.tileIdToZxy(PMTiles.zxyToTileId(z, 0, 0));
      assertArrayEquals(new long[] {z, 0, 0}, tl);
      var tr = PMTiles.tileIdToZxy(PMTiles.zxyToTileId(z, dim, 0));
      assertArrayEquals(new long[] {z, dim, 0}, tr);
      var bl = PMTiles.tileIdToZxy(PMTiles.zxyToTileId(z, 0, dim));
      assertArrayEquals(new long[] {z, 0, dim}, bl);
      var br = PMTiles.tileIdToZxy(PMTiles.zxyToTileId(z, dim, dim));
      assertArrayEquals(new long[] {z, dim, dim}, br);
    }
  }

  @Test
  void invalidTiles() {
    assertThrows(RuntimeException.class, () -> PMTiles.tileIdToZxy(9007199254740991L));
    assertThrows(RuntimeException.class, () -> PMTiles.zxyToTileId(27, 0, 0));
    assertThrows(RuntimeException.class, () -> PMTiles.zxyToTileId(0, 1, 1));
  }

  @Test
  void decodeHeader() throws IOException {
    var file = TestFiles.resolve("pmtiles/test_fixture_1.pmtiles");
    try (var channel = Files.newByteChannel(file)) {
      var buffer = ByteBuffer.allocate(127);
      channel.read(buffer);
      buffer.flip();
      var header = PMTiles.decodeHeader(buffer);
      assertEquals(header.getRootDirectoryOffset(), 127);
      assertEquals(header.getRootDirectoryLength(), 25);
      assertEquals(header.getJsonMetadataOffset(), 152);
      assertEquals(header.getJsonMetadataLength(), 247);
      assertEquals(header.getLeafDirectoryOffset(), 0);
      assertEquals(header.getLeafDirectoryLength(), 0);
      assertEquals(header.getTileDataOffset(), 399);
      assertEquals(header.getTileDataLength(), 69);
      assertEquals(header.getNumAddressedTiles(), 1);
      assertEquals(header.getNumTileEntries(), 1);
      assertEquals(header.getNumTileContents(), 1);
      assertFalse(header.isClustered());
      assertEquals(header.getInternalCompression(), Compression.Gzip);
      assertEquals(header.getTileCompression(), Compression.Gzip);
      assertEquals(header.getTileType(), TileType.Mvt);
      assertEquals(header.getMinZoom(), 0);
      assertEquals(header.getMaxZoom(), 0);
      assertEquals(header.getMinLon(), 0);
      assertEquals(header.getMinLat(), 0);
      assertEquals(Math.round(header.getMaxLon()), 1);
      assertEquals(Math.round(header.getMaxLat()), 1);
    }
  }

  @Test
  void encodeHeader() throws IOException {
    var etag = "1";
    var buffer = ByteBuffer.allocate(127);
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
        Compression.Gzip,
        Compression.Gzip,
        TileType.Mvt,
        0,
        0,
        0,
        1,
        1,
        0,
        0,
        0,
        0
    );
    PMTiles.encodeHeader(header, buffer);
    var header2 = PMTiles.decodeHeader(buffer);
    assertEquals(header, header2);
  }

  @Test
  void searchForMissingEntry() {
    var entries = new ArrayList<Entry>();
    assertEquals(PMTiles.findTile(entries, 101), null);
  }

  @Test
  void searchForFirstEntry() {
    var entry = new Entry(100, 1, 1, 1);
    var entries = new ArrayList<Entry>();
    entries.add(entry);
    assertEquals(PMTiles.findTile(entries, 100), entry);
  }

  @Test
  void searchWithRunLength() {
    var entry = new Entry(3, 3, 1, 2);
    var entries = new ArrayList<Entry>();
    entries.add(entry);
    entries.add(new Entry(5, 5, 1, 2));
    assertEquals(PMTiles.findTile(entries, 4), entry);
  }

  @Test
  void searchWithMultipleTileEntries() {
    var entries = new ArrayList<Entry>();
    entries.add(new Entry(100, 1, 1, 2));
    var entry = PMTiles.findTile(entries, 101);
    assertEquals(entry.getOffset(), 1);
    assertEquals(entry.getLength(), 1);

    entries = new ArrayList<Entry>();
    entries.add(new Entry(100, 1, 1, 1));
    entries.add(new Entry(150, 2, 2, 2));
    entry = PMTiles.findTile(entries, 151);
    assertEquals(entry.getOffset(), 2);
    assertEquals(entry.getLength(), 2);

    entries = new ArrayList<>();
    entries.add(new Entry(50, 1, 1, 2));
    entries.add(new Entry(100, 2, 2, 1));
    entries.add(new Entry(150, 3, 3, 1));
    entry = PMTiles.findTile(entries, 51);
    assertEquals(entry.getOffset(), 1);
    assertEquals(entry.getLength(), 1);
  }

  @Test
  void leafSearch() {
    var entries = new ArrayList<Entry>();
    entries.add(new Entry(100, 1, 1, 0));
    var entry = PMTiles.findTile(entries, 150);
    assertEquals(entry.getOffset(), 1);
    assertEquals(entry.getLength(), 1);
  }

  @Test
  void reader() throws InvalidProtocolBufferException, InterruptedException {
    var reader = new PMTilesReader(TestFiles.resolve("pmtiles/test_fixture_1.pmtiles"));
    var header = reader.getHeader();
    assertEquals(header.getRootDirectoryOffset(), 127);
    var rootDirectory = reader.getRootDirectory();
    assertEquals(rootDirectory.size(), 1);
    var entry = rootDirectory.get(0);
    ByteBuffer buffer = reader.getTile(0,0,0);

    VectorTileViewer viewer = new VectorTileViewer();
    var parsed = org.apache.baremaps.mvt.binary.VectorTile.Tile.parseFrom(buffer);
    var tile = new VectorTileDecoder().decodeTile(parsed);
    JFrame f = new JFrame("Vector Tile Viewer");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.add(new TilePanel(tile, 1000));
    f.pack();
    f.setVisible(true);

    Thread.sleep(10000);
  }


}
