package com.baremaps.osm.reader.pbf;

import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.reader.pbf.HeaderBlock.Builder;
import com.baremaps.osm.stream.BatchSpliterator;
import com.baremaps.osm.stream.StreamProgress;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FileBlockStreamer {

  public FileBlockStreamer() {
  }

  public Stream<FileBlock> stream(Path path, boolean parallel, boolean progress) throws IOException {
    Spliterator<Blob> spliterator = new BlobSpliterator(Files.newInputStream(path));
    if (parallel) {
      spliterator = new BatchSpliterator<>(spliterator, 1);
    }
    Stream<Blob> stream = StreamSupport.stream(spliterator, true);
    if (progress) {
      stream = stream.peek(new StreamProgress<>(Files.size(path), b -> b.size()));
    }
    return stream.map(FileBlockStreamer::toFileBlock);
  }

  private static FileBlock toFileBlock(Blob blob) {
    try {
      switch (blob.header().getType()) {
        case "OSMHeader":
          Osmformat.HeaderBlock headerBlock = Osmformat.HeaderBlock.parseFrom(blob.data());
          return new Builder(headerBlock).build();
        case "OSMData":
          Osmformat.PrimitiveBlock primitiveBlock = Osmformat.PrimitiveBlock.parseFrom(blob.data());
          return new DataBlock.Builder(primitiveBlock).build();
        default:
          return null;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
