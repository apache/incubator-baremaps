package com.baremaps.osm.pbf;

import com.baremaps.osm.domain.Blob;
import com.baremaps.osm.domain.Block;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.zip.DataFormatException;

public class BlockReader {

  private final Blob blob;

  public BlockReader(Blob blob) {
    this.blob = blob;
  }

  public Block readBlock() throws DataFormatException, InvalidProtocolBufferException {
    switch (blob.header().getType()) {
      case "OSMHeader":
        return BlobUtils.readHeaderBlock(blob);
      case "OSMData":
        return BlobUtils.readDataBlock(blob);
      default:
        throw new RuntimeException("Unknown blob type");
    }
  }



}
