package io.gazetteer.osm.osmpbf;

import com.google.protobuf.ByteString;
import io.gazetteer.osm.stream.BatchSpliterator;
import org.openstreetmap.osmosis.osmbinary.Fileformat;
import org.openstreetmap.osmosis.osmbinary.Fileformat.Blob;

import java.io.DataInputStream;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class FileBlockSpliterator extends BatchSpliterator<FileBlock> {

  protected final DataInputStream input;

  public FileBlockSpliterator(DataInputStream input) {
    super(10, IMMUTABLE | SUBSIZED);
    this.input = input;
  }

  @Override
  public boolean tryAdvance(Consumer<? super FileBlock> action) {
    try {
      int headerSize = input.readInt();
      byte[] headerData = new byte[headerSize];
      input.readFully(headerData);
      Fileformat.BlobHeader header = Fileformat.BlobHeader.parseFrom(headerData);
      int blobSize = header.getDatasize();
      byte[] blobData = new byte[blobSize];
      input.readFully(blobData);
      Fileformat.Blob blob = Fileformat.Blob.parseFrom(blobData);
      action.accept(new FileBlock(FileBlock.Type.valueOf(header.getType()), header.getIndexdata(), data(blob)));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private ByteString data(Blob blob) throws DataFormatException {
    if (blob.hasRaw()) {
      return blob.getRaw();
    } else if (blob.hasZlibData()) {
      byte[] bytes = new byte[blob.getRawSize()];
      Inflater inflater = new Inflater();
      inflater.setInput(blob.getZlibData().toByteArray());
      inflater.inflate(bytes);
      inflater.end();
      return ByteString.copyFrom(bytes);
    } else {
      throw new DataFormatException("Unsupported toPrimitiveBlock format");
    }
  }

}
