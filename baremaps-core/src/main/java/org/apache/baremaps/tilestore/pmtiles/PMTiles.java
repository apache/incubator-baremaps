package org.apache.baremaps.tilestore.pmtiles;

import com.google.common.math.LongMath;

import java.nio.ByteBuffer;

public class PMTiles {

    public static long toNum(long low, long high) {
        return high * 0x100000000L + low;
    }

    public static long readVarintRemainder(long l, ByteBuffer buf) {
        long h, b;
        b = buf.get() & 0xff;
        h = (b & 0x70) >> 4;
        if (b < 0x80) {
            return toNum(l, h);
        }
        b = buf.get() & 0xff;
        h |= (b & 0x7f) << 3;
        if (b < 0x80) {
            return toNum(l, h);
        }
        b = buf.get() & 0xff;
        h |= (b & 0x7f) << 10;
        if (b < 0x80) {
            return toNum(l, h);
        }
        b = buf.get() & 0xff;
        h |= (b & 0x7f) << 17;
        if (b < 0x80) {
            return toNum(l, h);
        }
        b = buf.get() & 0xff;
        h |= (b & 0x7f) << 24;
        if (b < 0x80) {
            return toNum(l, h);
        }
        b = buf.get() & 0xff;
        h |= (b & 0x01) << 31;
        if (b < 0x80) {
            return toNum(l, h);
        }
        throw new RuntimeException("Expected varint not more than 10 bytes");
    }

    public static long readVarint(ByteBuffer buf) {
        long val, b;
        b = buf.get() & 0xff;
        val = b & 0x7f;
        if (b < 0x80) {
            return val;
        }
        b = buf.get() & 0xff;
        val |= (b & 0x7f) << 7;
        if (b < 0x80) {
            return val;
        }
        b = buf.get() & 0xff;
        val |= (b & 0x7f) << 14;
        if (b < 0x80) {
            return val;
        }
        b = buf.get() & 0xff;
        val |= (b & 0x7f) << 21;
        if (b < 0x80) {
            return val;
        }
        val |= (b & 0x0f) << 28;
        return readVarintRemainder(val, buf);
    }

    public static void rotate(long n, long[] xy, long rx, long ry) {
        if (ry == 0) {
            if (rx == 1) {
                xy[0] = n - 1 - xy[0];
                xy[1] = n - 1 - xy[1];
            }
            long t = xy[0];
            xy[0] = xy[1];
            xy[1] = t;
        }
    }

    public static long[] idOnLevel(int z, long pos) {
        long n = LongMath.pow(2, z);
        long rx, ry, t = pos;
        long[] xy = new long[]{0, 0};
        long s = 1;
        while (s < n) {
            rx = 1 & (t / 2);
            ry = 1 & (t ^ rx);
            rotate(s, xy, rx, ry);
            xy[0] += s * rx;
            xy[1] += s * ry;
            t = t / 4;
            s *= 2;
        }
        return new long[]{z, xy[0], xy[1]};
    }

    private static long[] tzValues = new long[]{
            0, 1, 5, 21, 85, 341, 1365, 5461, 21845, 87381, 349525, 1398101, 5592405,
            22369621, 89478485, 357913941, 1431655765, 5726623061L, 22906492245L,
            91625968981L, 366503875925L, 1466015503701L, 5864062014805L, 23456248059221L,
            93824992236885L, 375299968947541L, 1501199875790165L,
    };

    public static long zxyToTileId(int z, long x, long y) {
        if (z > 26) {
            throw new RuntimeException("Tile zoom level exceeds max safe number limit (26)");
        }
        if (x > Math.pow(2, z) - 1 || y > Math.pow(2, z) - 1) {
            throw new RuntimeException("tile x/y outside zoom level bounds");
        }
        long acc = tzValues[z];
        long n = LongMath.pow(2, z);
        long rx = 0;
        long ry = 0;
        long d = 0;
        long[] xy = new long[]{x, y};
        long s = n / 2;
        while (s > 0) {
            rx = (xy[0] & s) > 0 ? 1 : 0;
            ry = (xy[1] & s) > 0 ? 1 : 0;
            d += s * s * ((3 * rx) ^ ry);
            rotate(s, xy, rx, ry);
            s = s / 2;
        }
        return acc + d;
    }

    public static long[] tileIdToZxy(long i) {
        long acc = 0;
        for (int z = 0; z < 27; z++) {
            long numTiles = (0x1L << z) * (0x1L << z);
            if (acc + numTiles > i) {
                return idOnLevel(z, i - acc);
            }
            acc += numTiles;
        }
        throw new RuntimeException("Tile zoom level exceeds max safe number limit (26)");
    }
}
