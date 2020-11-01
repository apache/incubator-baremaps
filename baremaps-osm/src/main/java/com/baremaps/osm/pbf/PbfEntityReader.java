package com.baremaps.osm.pbf;

import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.EntityReader;
import com.baremaps.osm.stream.BatchSpliterator;
import com.baremaps.osm.stream.StreamException;
import com.baremaps.osm.stream.StreamProgress;
import com.google.common.collect.Streams;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.DataFormatException;

public class PbfEntityReader implements EntityReader {

  private final InputStream inputStream;

  public PbfEntityReader(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public Stream<Entity> entities() throws IOException {
    Spliterator<Blob> spliterator = new BlobSpliterator(inputStream);
    spliterator = new BatchSpliterator<>(spliterator, 1);
    try {
      return StreamSupport
          .stream(spliterator, true)
          .peek(new StreamProgress<>(inputStream.available(), b -> b.size()))
          .flatMap(this::readBlob);
    } catch (StreamException e) {
      throw new IOException(e.getCause());
    }
  }

  public Stream<Entity> readBlob(Blob blob) {
    try {
      switch (blob.header().getType()) {
        case "OSMHeader":
          Osmformat.HeaderBlock headerBlock = Osmformat.HeaderBlock.parseFrom(blob.data());
          return readHeaderBlock(headerBlock);
        case "OSMData":
          Osmformat.PrimitiveBlock dataBlock = Osmformat.PrimitiveBlock.parseFrom(blob.data());
          return readDataBlock(dataBlock);
        default:
          return Stream.empty();
      }
    } catch (InvalidProtocolBufferException | DataFormatException e) {
      throw new StreamException(e);
    }
  }

  public Stream<Entity> readHeaderBlock(Osmformat.HeaderBlock headerBlock) {
    HeaderBlockReader reader = new HeaderBlockReader(headerBlock);
    return Stream.of(reader.readHeader(), reader.readBounds());
  }

  public Stream<Entity> readDataBlock(Osmformat.PrimitiveBlock dataBlock) {
    DataBlockReader reader = new DataBlockReader(dataBlock);
    return Streams.concat(reader.readDenseNodes(), reader.readNodes(), reader.readWays(), reader.readRelations());
  }

}
