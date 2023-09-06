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

  enum Compression {
    Unknown,
    None,
    Gzip,
    Brotli,
    Zstd,
  }

  enum TileType {
    Unknown,
    Mvt,
    Png,
    Jpeg,
    Webp,
    Avif,
  }

  private static final int HEADER_SIZE_BYTES = 127;

  public record Header(
      int specVersion,
      long rootDirectoryOffset,
      long rootDirectoryLength,
      long jsonMetadataOffset,
      long jsonMetadataLength,
      long leafDirectoryOffset,
      long leafDirectoryLength,
      long tileDataOffset,
      long tileDataLength,
      long numAddressedTiles,
      long numTileEntries,
      long numTileContents,
      boolean clustered,
      Compression internalCompression,
      Compression tileCompression,
      TileType tileType,
      int minZoom,
      int maxZoom,
      double minLon,
      double minLat,
      double maxLon,
      double maxLat,
      int centerZoom,
      double centerLon,
      double centerLat,
      String etag) {
  }

  public static Header decodeHeader(ByteBuffer buf, String etag) {
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
        (double) buf.getInt(123) / 10000000,
        etag);
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
    buf.put(7, (byte) header.specVersion);
    buf.putLong(8, header.rootDirectoryOffset);
    buf.putLong(16, header.rootDirectoryLength);
    buf.putLong(24, header.jsonMetadataOffset);
    buf.putLong(32, header.jsonMetadataLength);
    buf.putLong(40, header.leafDirectoryOffset);
    buf.putLong(48, header.leafDirectoryLength);
    buf.putLong(56, header.tileDataOffset);
    buf.putLong(64, header.tileDataLength);
    buf.putLong(72, header.numAddressedTiles);
    buf.putLong(80, header.numTileEntries);
    buf.putLong(88, header.numTileContents);
    buf.put(96, (byte) (header.clustered ? 1 : 0));
    buf.put(97, (byte) header.internalCompression.ordinal());
    buf.put(98, (byte) header.tileCompression.ordinal());
    buf.put(99, (byte) header.tileType.ordinal());
    buf.put(100, (byte) header.minZoom);
    buf.put(101, (byte) header.maxZoom);
    buf.putInt(102, (int) (header.minLon * 10000000));
    buf.putInt(106, (int) (header.minLat * 10000000));
    buf.putInt(110, (int) (header.maxLon * 10000000));
    buf.putInt(114, (int) (header.maxLat * 10000000));
    buf.put(118, (byte) header.centerZoom);
    buf.putInt(119, (int) (header.centerLon * 10000000));
    buf.putInt(123, (int) (header.centerLat * 10000000));
  }

  public static class Entry {
    private long tileId;
    private long offset;
    private long length;
    private long runLength;

    public Entry() {

    }

    public Entry(long tileId, long offset, long length, long runLength) {
      this.tileId = tileId;
      this.offset = offset;
      this.length = length;
      this.runLength = runLength;
    }

    public long getTileId() {
      return tileId;
    }

    public void setTileId(long tileId) {
      this.tileId = tileId;
    }

    public long getOffset() {
      return offset;
    }

    public void setOffset(long offset) {
      this.offset = offset;
    }

    public long getLength() {
      return length;
    }

    public void setLength(long length) {
      this.length = length;
    }

    public long getRunLength() {
      return runLength;
    }

    public void setRunLength(long runLength) {
      this.runLength = runLength;
    }
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
        encodeVarInt(buffer, prevEntry.offset + prevEntry.length + 1);
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
        entries.get(i).setOffset(prevEntry.offset + prevEntry.length);;
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
      if (entries.get(n).runLength == 0) {
        return entries.get(n);
      }
      if (tileId - entries.get(n).tileId < entries.get(n).runLength) {
        return entries.get(n);
      }
    }
    return null;
  }

}
