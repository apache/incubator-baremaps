package org.apache.baremaps.openstreetmap.pbf;

import org.apache.baremaps.openstreetmap.model.Blob;
import org.apache.baremaps.openstreetmap.model.Block;
import org.apache.baremaps.stream.StreamException;

import java.util.function.Function;

public class BlobToBlockFunction implements Function<Blob, Block> {

  @Override
  public Block apply(Blob blob) {
    try {
      switch (blob.header().getType()) {
        case "OSMHeader":
          return new HeaderBlockReader(blob).read();
        case "OSMData":
          return new DataBlockReader(blob).read();
        default:
          throw new StreamException("Unknown blob type");
      }
    } catch (StreamException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new StreamException(exception);
    }
  }
}
