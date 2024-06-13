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

import com.google.common.math.LongMath;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

class PMTilesUtils {

  private static final int HEADER_SIZE_BYTES = 127;

  private PMTilesUtils() {
    // Prevent instantiation
  }

  static long toNum(long low, long high) {
    return high * 0x100000000L + low;
  }

  static long readVarIntRemainder(InputStream input, long l)
      throws IOException {
    long h, b;
    b = input.read() & 0xff;
    h = (b & 0x70) >> 4;
    if (b < 0x80) {
      return toNum(l, h);
    }
    b = input.read() & 0xff;
    h |= (b & 0x7f) << 3;
    if (b < 0x80) {
      return toNum(l, h);
    }
    b = input.read() & 0xff;
    h |= (b & 0x7f) << 10;
    if (b < 0x80) {
      return toNum(l, h);
    }
    b = input.read() & 0xff;
    h |= (b & 0x7f) << 17;
    if (b < 0x80) {
      return toNum(l, h);
    }
    b = input.read() & 0xff;
    h |= (b & 0x7f) << 24;
    if (b < 0x80) {
      return toNum(l, h);
    }
    b = input.read() & 0xff;
    h |= (b & 0x01) << 31;
    if (b < 0x80) {
      return toNum(l, h);
    }
    throw new RuntimeException("Expected varint not more than 10 bytes");
  }

  static int writeVarInt(OutputStream output, long value)
      throws IOException {
    int n = 1;
    while (value >= 0x80) {
      output.write((byte) (value | 0x80));
      value >>>= 7;
      n++;
    }
    output.write((byte) value);
    return n;
  }

  static long readVarInt(InputStream input) throws IOException {
    long val, b;
    b = input.read() & 0xff;
    val = b & 0x7f;
    if (b < 0x80) {
      return val;
    }
    b = input.read() & 0xff;
    val |= (b & 0x7f) << 7;
    if (b < 0x80) {
      return val;
    }
    b = input.read() & 0xff;
    val |= (b & 0x7f) << 14;
    if (b < 0x80) {
      return val;
    }
    b = input.read() & 0xff;
    val |= (b & 0x7f) << 21;
    if (b < 0x80) {
      return val;
    }
    val |= (b & 0x0f) << 28;
    return readVarIntRemainder(input, val);
  }

  static void rotate(long n, long[] xy, long rx, long ry) {
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

  static long[] idOnLevel(int z, long pos) {
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

  private static final long[] tzValues = new long[] {
      0, 1, 5, 21, 85, 341, 1365, 5461, 21845, 87381, 349525, 1398101, 5592405,
      22369621, 89478485, 357913941, 1431655765, 5726623061L, 22906492245L,
      91625968981L, 366503875925L, 1466015503701L, 5864062014805L, 23456248059221L,
      93824992236885L, 375299968947541L, 1501199875790165L,
  };

  static long zxyToTileId(int z, long x, long y) {
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

  static long[] tileIdToZxy(long i) {
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

  static Header deserializeHeader(InputStream input) throws IOException {
    byte[] bytes = new byte[HEADER_SIZE_BYTES];
    var num = input.read(bytes);
    if (num != HEADER_SIZE_BYTES) {
      throw new IOException("Invalid header size");
    }
    var buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    buffer.position(7);
    return new Header(
        buffer.get(),
        buffer.getLong(),
        buffer.getLong(),
        buffer.getLong(),
        buffer.getLong(),
        buffer.getLong(),
        buffer.getLong(),
        buffer.getLong(),
        buffer.getLong(),
        buffer.getLong(),
        buffer.getLong(),
        buffer.getLong(),
        buffer.get() == 1,
        Compression.values()[buffer.get()],
        Compression.values()[buffer.get()],
        TileType.values()[buffer.get()],
        buffer.get(),
        buffer.get(),
        (double) buffer.getInt() / 10000000,
        (double) buffer.getInt() / 10000000,
        (double) buffer.getInt() / 10000000,
        (double) buffer.getInt() / 10000000,
        buffer.get(),
        (double) buffer.getInt() / 10000000,
        (double) buffer.getInt() / 10000000);
  }

  static byte[] serializeHeader(Header header) {
    var buffer = ByteBuffer.allocate(HEADER_SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN);
    buffer.put((byte) 0x50);
    buffer.put((byte) 0x4D);
    buffer.put((byte) 0x54);
    buffer.put((byte) 0x69);
    buffer.put((byte) 0x6C);
    buffer.put((byte) 0x65);
    buffer.put((byte) 0x73);
    buffer.put((byte) header.getSpecVersion());
    buffer.putLong(header.getRootDirectoryOffset());
    buffer.putLong(header.getRootDirectoryLength());
    buffer.putLong(header.getJsonMetadataOffset());
    buffer.putLong(header.getJsonMetadataLength());
    buffer.putLong(header.getLeafDirectoryOffset());
    buffer.putLong(header.getLeafDirectoryLength());
    buffer.putLong(header.getTileDataOffset());
    buffer.putLong(header.getTileDataLength());
    buffer.putLong(header.getNumAddressedTiles());
    buffer.putLong(header.getNumTileEntries());
    buffer.putLong(header.getNumTileContents());
    buffer.put((byte) (header.isClustered() ? 1 : 0));
    buffer.put((byte) header.getInternalCompression().ordinal());
    buffer.put((byte) header.getTileCompression().ordinal());
    buffer.put((byte) header.getTileType().ordinal());
    buffer.put((byte) header.getMinZoom());
    buffer.put((byte) header.getMaxZoom());
    buffer.putInt((int) (header.getMinLon() * 10000000));
    buffer.putInt((int) (header.getMinLat() * 10000000));
    buffer.putInt((int) (header.getMaxLon() * 10000000));
    buffer.putInt((int) (header.getMaxLat() * 10000000));
    buffer.put((byte) header.getCenterZoom());
    buffer.putInt((int) (header.getCenterLon() * 10000000));
    buffer.putInt((int) (header.getCenterLat() * 10000000));
    buffer.flip();
    return buffer.array();
  }

  static void serializeEntries(OutputStream output, List<Entry> entries)
      throws IOException {
    var buffer = ByteBuffer.allocate(entries.size() * 48);
    writeVarInt(output, entries.size());
    long lastId = 0;
    for (Entry entry : entries) {
      writeVarInt(output, entry.getTileId() - lastId);
      lastId = entry.getTileId();
    }
    for (Entry entry : entries) {
      writeVarInt(output, entry.getRunLength());
    }
    for (Entry entry : entries) {
      writeVarInt(output, entry.getLength());
    }
    for (int i = 0; i < entries.size(); i++) {
      Entry entry = entries.get(i);
      if (i > 0
          && entry.getOffset() == entries.get(i - 1).getOffset() + entries.get(i - 1).getLength()) {
        writeVarInt(output, 0);
      } else {
        writeVarInt(output, entry.getOffset() + 1);
      }
    }
    buffer.flip();
    output.write(buffer.array(), 0, buffer.limit());
  }

  static List<Entry> deserializeEntries(InputStream buffer)
      throws IOException {
    long numEntries = readVarInt(buffer);
    List<Entry> entries = new ArrayList<>((int) numEntries);
    long lastId = 0;
    for (int i = 0; i < numEntries; i++) {
      long value = readVarInt(buffer);
      lastId = lastId + value;
      Entry entry = new Entry();
      entry.setTileId(lastId);
      entries.add(entry);
    }
    for (int i = 0; i < numEntries; i++) {
      long value = readVarInt(buffer);
      entries.get(i).setRunLength(value);
    }
    for (int i = 0; i < numEntries; i++) {
      long value = readVarInt(buffer);
      entries.get(i).setLength(value);
    }
    for (int i = 0; i < numEntries; i++) {
      long value = readVarInt(buffer);
      if (value == 0 && i > 0) {
        Entry prevEntry = entries.get(i - 1);
        entries.get(i).setOffset(prevEntry.getOffset() + prevEntry.getLength());;
      } else {
        entries.get(i).setOffset(value - 1);
      }
    }
    return entries;
  }

  static Entry findTile(List<Entry> entries, long tileId) {
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

  static Directories buildRootLeaves(List<Entry> entries, int leafSize,
      Compression compression) throws IOException {
    var rootEntries = new ArrayList<Entry>();
    var numLeaves = 0;
    byte[] leavesBytes;
    byte[] rootBytes;

    try (var leavesOutput = new ByteArrayOutputStream()) {
      for (var i = 0; i < entries.size(); i += leafSize) {
        numLeaves++;
        var end = i + leafSize;
        if (i + leafSize > entries.size()) {
          end = entries.size();
        }
        var offset = leavesOutput.size();
        try (var leafOutput = new ByteArrayOutputStream()) {
          try (var compressedLeafOutput = compression.compress(leafOutput)) {
            serializeEntries(compressedLeafOutput, entries.subList(i, end));
          }
          var length = leafOutput.size();
          rootEntries.add(new Entry(entries.get(i).getTileId(), offset, length, 0));
          leavesOutput.write(leafOutput.toByteArray());
        }
      }
      leavesBytes = leavesOutput.toByteArray();
    }

    try (var rootOutput = new ByteArrayOutputStream()) {
      try (var compressedRootOutput = compression.compress(rootOutput)) {
        serializeEntries(compressedRootOutput, rootEntries);
      }
      rootBytes = rootOutput.toByteArray();
    }

    return new Directories(rootBytes, leavesBytes, numLeaves);
  }

  static Directories optimizeDirectories(List<Entry> entries, int targetRootLength,
      Compression compression)
      throws IOException {
    if (entries.size() < 16384) {
      try (var rootOutput = new ByteArrayOutputStream()) {
        try (var compressedOutput = compression.compress(rootOutput)) {
          serializeEntries(compressedOutput, entries);
        }
        byte[] rootBytes = rootOutput.toByteArray();
        if (rootBytes.length <= targetRootLength) {
          return new Directories(rootBytes, new byte[] {}, 0);
        }
      }
    }

    double leafSize = Math.max((double) entries.size() / 3500, 4096);
    for (;;) {
      var directories = buildRootLeaves(entries, (int) leafSize, compression);
      if (directories.getRoot().length <= targetRootLength) {
        return directories;
      }
      leafSize = leafSize * 1.2;
    }
  }
}
