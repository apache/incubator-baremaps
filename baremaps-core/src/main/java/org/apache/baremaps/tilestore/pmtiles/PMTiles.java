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

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import com.google.common.math.LongMath;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PMTiles {

  public static long toNum(long low, long high) {
    return high * 0x100000000L + low;
  }

  public static long readVarIntRemainder(LittleEndianDataInputStream input, long l)
      throws IOException {
    long h, b;
    b = input.readByte() & 0xff;
    h = (b & 0x70) >> 4;
    if (b < 0x80) {
      return toNum(l, h);
    }
    b = input.readByte() & 0xff;
    h |= (b & 0x7f) << 3;
    if (b < 0x80) {
      return toNum(l, h);
    }
    b = input.readByte() & 0xff;
    h |= (b & 0x7f) << 10;
    if (b < 0x80) {
      return toNum(l, h);
    }
    b = input.readByte() & 0xff;
    h |= (b & 0x7f) << 17;
    if (b < 0x80) {
      return toNum(l, h);
    }
    b = input.readByte() & 0xff;
    h |= (b & 0x7f) << 24;
    if (b < 0x80) {
      return toNum(l, h);
    }
    b = input.readByte() & 0xff;
    h |= (b & 0x01) << 31;
    if (b < 0x80) {
      return toNum(l, h);
    }
    throw new RuntimeException("Expected varint not more than 10 bytes");
  }

  public static int writeVarInt(LittleEndianDataOutputStream output, long value)
      throws IOException {
    int n = 1;
    while (value >= 0x80) {
      output.writeByte((byte) (value | 0x80));
      value >>>= 7;
      n++;
    }
    output.writeByte((byte) value);
    return n;
  }

  public static long readVarInt(LittleEndianDataInputStream input) throws IOException {
    long val, b;
    b = input.readByte() & 0xff;
    val = b & 0x7f;
    if (b < 0x80) {
      return val;
    }
    b = input.readByte() & 0xff;
    val |= (b & 0x7f) << 7;
    if (b < 0x80) {
      return val;
    }
    b = input.readByte() & 0xff;
    val |= (b & 0x7f) << 14;
    if (b < 0x80) {
      return val;
    }
    b = input.readByte() & 0xff;
    val |= (b & 0x7f) << 21;
    if (b < 0x80) {
      return val;
    }
    val |= (b & 0x0f) << 28;
    return readVarIntRemainder(input, val);
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

  public static Header deserializeHeader(LittleEndianDataInputStream input) throws IOException {
    input.skipBytes(7);
    return new Header(
        input.readByte(),
        input.readLong(),
        input.readLong(),
        input.readLong(),
        input.readLong(),
        input.readLong(),
        input.readLong(),
        input.readLong(),
        input.readLong(),
        input.readLong(),
        input.readLong(),
        input.readLong(),
        input.readByte() == 1,
        Compression.values()[input.readByte()],
        Compression.values()[input.readByte()],
        TileType.values()[input.readByte()],
        input.readByte(),
        input.readByte(),
        (double) input.readInt() / 10000000,
        (double) input.readInt() / 10000000,
        (double) input.readInt() / 10000000,
        (double) input.readInt() / 10000000,
        input.readByte(),
        (double) input.readInt() / 10000000,
        (double) input.readInt() / 10000000);
  }

  public static void serializeHeader(LittleEndianDataOutputStream output, Header header)
      throws IOException {
    output.writeByte((byte) 0x50);
    output.writeByte((byte) 0x4D);
    output.writeByte((byte) 0x54);
    output.writeByte((byte) 0x69);
    output.writeByte((byte) 0x6C);
    output.writeByte((byte) 0x65);
    output.writeByte((byte) 0x73);
    output.writeByte((byte) header.getSpecVersion());
    output.writeLong(header.getRootDirectoryOffset());
    output.writeLong(header.getRootDirectoryLength());
    output.writeLong(header.getJsonMetadataOffset());
    output.writeLong(header.getJsonMetadataLength());
    output.writeLong(header.getLeafDirectoryOffset());
    output.writeLong(header.getLeafDirectoryLength());
    output.writeLong(header.getTileDataOffset());
    output.writeLong(header.getTileDataLength());
    output.writeLong(header.getNumAddressedTiles());
    output.writeLong(header.getNumTileEntries());
    output.writeLong(header.getNumTileContents());
    output.writeByte((byte) (header.isClustered() ? 1 : 0));
    output.writeByte((byte) header.getInternalCompression().ordinal());
    output.writeByte((byte) header.getTileCompression().ordinal());
    output.writeByte((byte) header.getTileType().ordinal());
    output.writeByte((byte) header.getMinZoom());
    output.writeByte((byte) header.getMaxZoom());
    output.writeInt((int) (header.getMinLon() * 10000000));
    output.writeInt((int) (header.getMinLat() * 10000000));
    output.writeInt((int) (header.getMaxLon() * 10000000));
    output.writeInt((int) (header.getMaxLat() * 10000000));
    output.writeByte((byte) header.getCenterZoom());
    output.writeInt((int) (header.getCenterLon() * 10000000));
    output.writeInt((int) (header.getCenterLat() * 10000000));
  }

  public static void serializeEntries(LittleEndianDataOutputStream output, List<Entry> entries)
      throws IOException {
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
  }

  public static List<Entry> deserializeEntries(LittleEndianDataInputStream buffer)
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

  public static Directories buildRootLeaves(List<Entry> entries, int leafSize) throws IOException {
    var rootEntries = new ArrayList<Entry>();
    var numLeaves = 0;
    byte[] leavesBytes;
    byte[] rootBytes;

    try (var leavesOutput = new ByteArrayOutputStream();
        var leavesDataOutput = new LittleEndianDataOutputStream(leavesOutput)) {
      for (var i = 0; i < entries.size(); i += leafSize) {
        numLeaves++;
        var end = i + leafSize;
        if (i + leafSize > entries.size()) {
          end = entries.size();
        }

        var offset = leavesOutput.size();
        serializeEntries(leavesDataOutput, entries.subList(i, end));
        var length = leavesOutput.size();
        rootEntries.add(new Entry(entries.get(i).getTileId(), offset, length, 0));
      }

      leavesBytes = leavesOutput.toByteArray();
    }

    try (var rootOutput = new ByteArrayOutputStream();
        var rootDataOutput = new LittleEndianDataOutputStream(rootOutput)) {
      serializeEntries(rootDataOutput, rootEntries);
      rootBytes = rootOutput.toByteArray();
    }

    return new Directories(rootBytes, leavesBytes, numLeaves);
  }

  public static Directories optimizeDirectories(List<Entry> entries, int targetRootLenght)
      throws IOException {
    if (entries.size() < 16384) {
      byte[] rootBytes;
      try (var rootOutput = new ByteArrayOutputStream();
          var rootDataOutput = new LittleEndianDataOutputStream(rootOutput)) {
        serializeEntries(rootDataOutput, entries);
        rootBytes = rootOutput.toByteArray();
      }
      if (rootBytes.length <= targetRootLenght) {
        return new Directories(rootBytes, new byte[] {}, 0);
      }
    }

    double leafSize = (double) entries.size() / 3500;
    if (leafSize < 4096) {
      leafSize = 4096;
    }
    for (;;) {
      var directories = buildRootLeaves(entries, (int) leafSize);
      if (directories.getRoot().length <= targetRootLenght) {
        return directories;
      }
      leafSize = leafSize * 1.2;
    }
  }
}
