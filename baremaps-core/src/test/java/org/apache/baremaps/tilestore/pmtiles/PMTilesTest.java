package org.apache.baremaps.tilestore.pmtiles;

import com.google.common.math.LongMath;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class PMTilesTest {

    @Test
    void varint() {
        var b = ByteBuffer.wrap(new byte[]{
                (byte) 0, (byte) 1,
                (byte) 127, (byte) 0xe5,
                (byte) 0x8e, (byte) 0x26
        });
        assertEquals(PMTiles.readVarint(b), 0);
        assertEquals(PMTiles.readVarint(b), 1);
        assertEquals(PMTiles.readVarint(b), 127);
        assertEquals(PMTiles.readVarint(b), 624485);
        b = ByteBuffer.wrap(new byte[]{
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
        assertArrayEquals(PMTiles.tileIdToZxy(0), new long[]{0, 0, 0});
        assertArrayEquals(PMTiles.tileIdToZxy(1), new long[]{1, 0, 0});
        assertArrayEquals(PMTiles.tileIdToZxy(2), new long[]{1, 0, 1});
        assertArrayEquals(PMTiles.tileIdToZxy(3), new long[]{1, 1, 1});
        assertArrayEquals(PMTiles.tileIdToZxy(4), new long[]{1, 1, 0});
        assertArrayEquals(PMTiles.tileIdToZxy(5), new long[]{2, 0, 0});
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
            assertArrayEquals(new long[]{z, 0, 0}, tl);
            var tr = PMTiles.tileIdToZxy(PMTiles.zxyToTileId(z, dim, 0));
            assertArrayEquals(new long[]{z, dim, 0}, tr);
            var bl = PMTiles.tileIdToZxy(PMTiles.zxyToTileId(z, 0, dim));
            assertArrayEquals(new long[]{z, 0, dim}, bl);
            var br = PMTiles.tileIdToZxy(PMTiles.zxyToTileId(z, dim, dim));
            assertArrayEquals(new long[]{z, dim, dim}, br);
        }
    }

    @Test
    void invalidTiles() {
        assertThrows(RuntimeException.class, () -> PMTiles.tileIdToZxy(9007199254740991L));
        assertThrows(RuntimeException.class, () -> PMTiles.zxyToTileId(27, 0, 0));
        assertThrows(RuntimeException.class, () -> PMTiles.zxyToTileId(0, 1, 1));
    }

}