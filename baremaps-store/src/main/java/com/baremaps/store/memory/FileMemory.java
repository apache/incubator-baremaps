package com.baremaps.store.memory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileMemory implements Memory {

  private final int segmentBytes;

  private final int segmentBits;

  private final long segmentMask;

  private final Path file;

  private final int capacity;

  private final FileChannel channel;

  private final List<MappedByteBuffer> segments = new ArrayList<>();

  public FileMemory() throws IOException {
    this(Files.createTempFile("tmp_", ".data"), 1 << 30);
  }

  public FileMemory(int segmentBytes) throws IOException {
    this(Files.createTempFile("tmp_", ".data"), segmentBytes);
  }

  public FileMemory(Path file, int segmentBytes) throws IOException {
    if ((segmentBytes & -segmentBytes) != segmentBytes) {
      throw new IllegalArgumentException("The segment size must be a power of 2");
    }
    this.segmentBytes = segmentBytes;
    this.segmentBits = (int) (Math.log(segmentBytes) / Math.log(2));
    this.segmentMask = (1L << segmentBits) - 1;
    this.file = file;
    this.capacity = segmentBytes;
    this.channel = FileChannel.open(
        file,
        StandardOpenOption.CREATE,
        StandardOpenOption.READ,
        StandardOpenOption.WRITE);
  }

  public final Path file() {
    return file;
  }

  @Override
  public int segmentBytes() {
    return segmentBytes;
  }

  @Override
  public long segmentBits() {
    return segmentBytes;
  }

  @Override
  public long segmentMask() {
    return segmentMask;
  }

  @Override
  public ByteBuffer segment(int index) {
    while (segments.size() <= index) {
      segments.add(null);
    }
    ByteBuffer segment = segments.get(index);
    if (segment == null) {
      segment = newSegment(index);
    }
    return segment;
  }

  synchronized private ByteBuffer newSegment(int index) {
    MappedByteBuffer buffer = segments.get(index);
    if (buffer == null) {
      try {
        buffer = channel.map(MapMode.READ_WRITE, index * (long) capacity, capacity);
        segments.set(index, buffer);
      } catch (IOException e) {
        throw new RuntimeException();
      }
    }
    return buffer;
  }

  @Override
  public void close() throws Exception {
    channel.close();
    for (MappedByteBuffer segment : segments) {
      segment.force();
    }
  }
}
