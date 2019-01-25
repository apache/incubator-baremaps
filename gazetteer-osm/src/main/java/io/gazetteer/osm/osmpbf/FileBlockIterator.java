package io.gazetteer.osm.osmpbf;

import com.google.protobuf.ByteString;
import org.openstreetmap.osmosis.osmbinary.Fileformat;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static com.google.common.base.Preconditions.checkNotNull;

public class FileBlockIterator implements Iterator<FileBlock> {

  private final DataInputStream input;

  public FileBlockIterator(DataInputStream input) {
    checkNotNull(input);
    this.input = input;
  }

  @Override
  public boolean hasNext() {
    try {
      return input.available() > 0;
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public FileBlock next() {
    try {
      int headerSize = input.readInt();
      byte[] headerData = new byte[headerSize];
      input.readFully(headerData);
      Fileformat.BlobHeader header = Fileformat.BlobHeader.parseFrom(headerData);

      int blobSize = header.getDatasize();
      byte[] blobData = new byte[blobSize];
      input.readFully(blobData);
      Fileformat.Blob blob = Fileformat.Blob.parseFrom(blobData);

      if (blob.hasRaw()) {
        return new FileBlock(header.getType(), header.getIndexdata(), blob.getRaw());
      } else if (blob.hasZlibData()) {
        byte[] raw = new byte[blob.getRawSize()];
        Inflater inflater = new Inflater();
        inflater.setInput(blob.getZlibData().toByteArray());
        try {
          inflater.inflate(raw);
        } catch (DataFormatException e) {
          throw new IOException(e);
        }
        inflater.end();
        return new FileBlock(header.getType(), header.getIndexdata(), ByteString.copyFrom(raw));
      } else {
        throw new IOException("Unsupported data format");
      }
    } catch (IOException e) {
      throw new NoSuchElementException(e.getMessage());
    }
  }
}
