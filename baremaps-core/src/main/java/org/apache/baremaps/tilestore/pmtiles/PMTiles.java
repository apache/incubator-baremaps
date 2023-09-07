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

import com.google.common.math.LongMath;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class PMTiles {

  public static long toNum(long low, long high) {
    return high * 0x100000000L + low;
  }

  public static long readVarIntRemainder(long l, ByteBuffer buf) {
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

  public static int encodeVarInt(ByteBuffer buf, long value) {
    int n = 1;
    while (value >= 0x80) {
      buf.put((byte) (value | 0x80));
      value >>>= 7;
      n++;
    }
    buf.put((byte) value);
    return n;
  }

  public static long decodeVarInt(ByteBuffer buf) {
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
    return readVarIntRemainder(val, buf);
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
    long[] xy = new long[] {0, 0};
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
    return new long[] {z, xy[0], xy[1]};
  }

  private static long[] tzValues = new long[] {
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
    long[] xy = new long[] {x, y};
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

  private static final int HEADER_SIZE_BYTES = 127;

  public static Header decodeHeader(ByteBuffer buf) {
    buf.order(ByteOrder.LITTLE_ENDIAN);
    return new Header(
        buf.get(7),
        buf.getLong(8),
        buf.getLong(16),
        buf.getLong(24),
        buf.getLong(32),
        buf.getLong(40),
        buf.getLong(48),
        buf.getLong(56),
        buf.getLong(64),
        buf.getLong(72),
        buf.getLong(80),
        buf.getLong(88),
        buf.get(96) == 1,
        Compression.values()[buf.get(97)],
        Compression.values()[buf.get(98)],
        TileType.values()[buf.get(99)],
        buf.get(100),
        buf.get(101),
        (double) buf.getInt(102) / 10000000,
        (double) buf.getInt(106) / 10000000,
        (double) buf.getInt(110) / 10000000,
        (double) buf.getInt(114) / 10000000,
        buf.get(118),
        (double) buf.getInt(119) / 10000000,
        (double) buf.getInt(123) / 10000000
    );
  }

  public static void encodeHeader(Header header, ByteBuffer buf) {
    buf.order(ByteOrder.LITTLE_ENDIAN);
    buf.put(0, (byte) 0x50);
    buf.put(1, (byte) 0x4D);
    buf.put(2, (byte) 0x54);
    buf.put(3, (byte) 0x69);
    buf.put(4, (byte) 0x6C);
    buf.put(5, (byte) 0x65);
    buf.put(6, (byte) 0x73);
    buf.put(7, (byte) header.getSpecVersion());
    buf.putLong(8, header.getRootDirectoryOffset());
    buf.putLong(16, header.getRootDirectoryLength());
    buf.putLong(24, header.getJsonMetadataOffset());
    buf.putLong(32, header.getJsonMetadataLength());
    buf.putLong(40, header.getLeafDirectoryOffset());
    buf.putLong(48, header.getLeafDirectoryLength());
    buf.putLong(56, header.getTileDataOffset());
    buf.putLong(64, header.getTileDataLength());
    buf.putLong(72, header.getNumAddressedTiles());
    buf.putLong(80, header.getNumTileEntries());
    buf.putLong(88, header.getNumTileContents());
    buf.put(96, (byte) (header.isClustered() ? 1 : 0));
    buf.put(97, (byte) header.getInternalCompression().ordinal());
    buf.put(98, (byte) header.getTileCompression().ordinal());
    buf.put(99, (byte) header.getTileType().ordinal());
    buf.put(100, (byte) header.getMinZoom());
    buf.put(101, (byte) header.getMaxZoom());
    buf.putInt(102, (int) (header.getMinLon() * 10000000));
    buf.putInt(106, (int) (header.getMinLat() * 10000000));
    buf.putInt(110, (int) (header.getMaxLon() * 10000000));
    buf.putInt(114, (int) (header.getMaxLat() * 10000000));
    buf.put(118, (byte) header.getCenterZoom());
    buf.putInt(119, (int) (header.getCenterLon() * 10000000));
    buf.putInt(123, (int) (header.getCenterLat() * 10000000));
  }

  public static void encodeDirectory(ByteBuffer buffer, List<Entry> entries) {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    encodeVarInt(buffer, entries.size());
    long lastId = 0;
    for (Entry entry : entries) {
      encodeVarInt(buffer, entry.getTileId() - lastId);
      lastId = entry.getTileId();
    }
    for (Entry entry : entries) {
      encodeVarInt(buffer, entry.getRunLength());
    }
    for (Entry entry : entries) {
      encodeVarInt(buffer, entry.getLength());
    }
    for (Entry entry : entries) {
      if (entry.getOffset() == 0 && entry.getLength() > 0) {
        Entry prevEntry = entries.get(entries.indexOf(entry) - 1);
        encodeVarInt(buffer, prevEntry.getOffset() + prevEntry.getLength() + 1);
      } else {
        encodeVarInt(buffer, entry.getOffset() + 1);
      }
    }
  }

  public static List<Entry> decodeDirectory(ByteBuffer buffer) {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    long numEntries = decodeVarInt(buffer);
    List<Entry> entries = new ArrayList<>((int) numEntries);
    long lastId = 0;
    for (int i = 0; i < numEntries; i++) {
      long value = decodeVarInt(buffer);
      lastId = lastId + value;
      Entry entry = new Entry();
      entry.setTileId(lastId);
      entries.add(entry);
    }
    for (int i = 0; i < numEntries; i++) {
      long value = decodeVarInt(buffer);
      entries.get(i).setRunLength(value);
    }
    for (int i = 0; i < numEntries; i++) {
      long value = decodeVarInt(buffer);
      entries.get(i).setLength(value);
    }
    for (int i = 0; i < numEntries; i++) {
      long value = decodeVarInt(buffer);
      if (value == 0 && i > 0) {
        Entry prevEntry = entries.get(i - 1);
        entries.get(i).setOffset(prevEntry.getOffset() + prevEntry.getLength());;
      } else {
        entries.get(i).setOffset(value - 1);
      }
    }
    return entries;
  }

  public static Entry findTile(List<Entry> entries, long tileId) {
    int m = 0;
    int n = entries.size() - 1;
    while (m <= n) {
      int k = (n + m) >> 1;
      long cmp = tileId - entries.get(k).getTileId();
      if (cmp > 0) {
        m = k + 1;
      } else if (cmp < 0) {
        n = k - 1;
      } else {
        return entries.get(k);
      }
    }

    // at this point, m > n
    if (n >= 0) {
      if (entries.get(n).getRunLength() == 0) {
        return entries.get(n);
      }
      if (tileId - entries.get(n).getTileId() < entries.get(n).getRunLength()) {
        return entries.get(n);
      }
    }
    return null;
  }

}
