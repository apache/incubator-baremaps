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
import org.apache.baremaps.testing.TestFiles;
import org.apache.baremaps.tilestore.pmtiles.PMTiles.Compression;
import org.apache.baremaps.tilestore.pmtiles.PMTiles.TileType;
import org.junit.jupiter.api.Test;

class PMTilesTest {

  @Test
  void varint() {
    var b = ByteBuffer.wrap(new byte[] {
        (byte) 0, (byte) 1,
        (byte) 127, (byte) 0xe5,
        (byte) 0x8e, (byte) 0x26
    });
    assertEquals(PMTiles.readVarint(b), 0);
    assertEquals(PMTiles.readVarint(b), 1);
    assertEquals(PMTiles.readVarint(b), 127);
    assertEquals(PMTiles.readVarint(b), 624485);
    b = ByteBuffer.wrap(new byte[] {
        (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0xff,
        (byte) 0xff, (byte) 0x0f,
    });
    assertEquals(PMTiles.readVarint(b), 9007199254740991L);
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
  void bytesToHeader() throws IOException {
    var file = TestFiles.resolve("pmtiles/test_fixture_1.pmtiles");
    try (var channel = Files.newByteChannel(file)) {
      var buffer = ByteBuffer.allocate(127);
      channel.read(buffer);
      buffer.flip();
      var header = PMTiles.bytesToHeader(buffer, "1");
      assertEquals(header.rootDirectoryOffset(), 127);
      assertEquals(header.rootDirectoryLength(), 25);
      assertEquals(header.jsonMetadataOffset(), 152);
      assertEquals(header.jsonMetadataLength(), 247);
      assertEquals(header.leafDirectoryOffset(), 0);
      assertEquals(header.leafDirectoryLength(), 0);
      assertEquals(header.tileDataOffset(), 399);
      assertEquals(header.tileDataLength(), 69);
      assertEquals(header.numAddressedTiles(), 1);
      assertEquals(header.numTileEntries(), 1);
      assertEquals(header.numTileContents(), 1);
      assertFalse(header.clustered());
      assertEquals(header.internalCompression(), Compression.Gzip);
      assertEquals(header.tileCompression(), Compression.Gzip);
      assertEquals(header.tileType(), TileType.Mvt);
      assertEquals(header.minZoom(), 0);
      assertEquals(header.maxZoom(), 0);
      assertEquals(header.minLon(), 0);
      assertEquals(header.minLat(), 0);
      assertEquals(Math.round(header.maxLon()), 1);
      assertEquals(Math.round(header.maxLat()), 1);
    }
  }

  @Test
  void headerToBytes() throws IOException {
    var etag = "1";
    var buffer = ByteBuffer.allocate(127);
    var header = new PMTiles.Header(
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
        0,
        etag);
    PMTiles.headerToBytes(header, buffer);
    var header2 = PMTiles.bytesToHeader(buffer, etag);
    assertEquals(header, header2);
  }

}
