package com.baremaps.osm.pbf;

import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.stream.StreamException;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

public class BlobReader {

  private final Blob blob;

  public BlobReader(Blob blob) {
    this.blob = blob;
  }

  public Stream<Entity> read() {
    try {
      switch (blob.header().getType()) {
        case "OSMHeader":
          Osmformat.HeaderBlock headerBlock = Osmformat.HeaderBlock.parseFrom(blob.data());
          HeaderBlockReader headerBlockReader = new HeaderBlockReader(headerBlock);
          Stream.Builder<Entity> headerStream = Stream.builder();
          headerStream.add(headerBlockReader.readHeader());
          Bound bound = headerBlockReader.readBounds();
          if (bound != null) {
            headerStream.add(bound);
          }
          return headerStream.build();
        case "OSMData":
          Osmformat.PrimitiveBlock dataBlock = Osmformat.PrimitiveBlock.parseFrom(blob.data());
          DataBlockReader dataBlockReader = new DataBlockReader(dataBlock);
          Stream.Builder<Entity> dataStream = Stream.builder();
          dataBlockReader.readDenseNodes(e -> dataStream.add(e));
          dataBlockReader.readNodes(e -> dataStream.add(e));
          dataBlockReader.readWays(e -> dataStream.add(e));
          dataBlockReader.readRelations(e -> dataStream.add(e));
          return dataStream.build();
        default:
          return Stream.empty();
      }
    } catch (InvalidProtocolBufferException | DataFormatException e) {
      throw new StreamException(e);
    }
  }

}
