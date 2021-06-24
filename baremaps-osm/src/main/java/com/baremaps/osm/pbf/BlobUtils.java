package com.baremaps.osm.pbf;

import com.baremaps.osm.domain.Blob;
import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.DataBlock;
import com.baremaps.osm.domain.HeaderBlock;
import com.baremaps.stream.StreamException;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.zip.DataFormatException;

public class BlobUtils {

  private BlobUtils() {

  }

  public static Block readBlock(Blob blob) {
    try {
      return new BlockReader(blob).readBlock();
    } catch (DataFormatException | InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }

  public static HeaderBlock readHeaderBlock(Blob blob) {
    try {
      return new HeaderBlockReader(blob).readHeaderBlock();
    } catch (DataFormatException | InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }

  public static DataBlock readDataBlock(Blob blob) {
    try {
      return new DataBlockReader(blob).readDataBlock();
    } catch (DataFormatException | InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }


}
