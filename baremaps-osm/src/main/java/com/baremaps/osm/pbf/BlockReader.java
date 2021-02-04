package com.baremaps.osm.pbf;

import com.baremaps.osm.domain.Blob;
import com.baremaps.osm.domain.Block;
import com.baremaps.stream.StreamException;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.zip.DataFormatException;

public class BlockReader {

  private final Blob blob;

  public BlockReader(Blob blob) {
    this.blob = blob;
  }

  public Block readBlock() {
    try {
      switch (blob.header().getType()) {
        case "OSMHeader":
          return new HeaderBlockReader(blob).readHeaderBlock();
        case "OSMData":
          return new DataBlockReader(blob).readDataBlock();
        default:
          throw new RuntimeException("Unknown blob type");
      }
    } catch (InvalidProtocolBufferException | DataFormatException e) {
      throw new StreamException(e);
    }
  }

}
