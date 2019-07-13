package io.gazetteer.osm.osmpbf;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.protobuf.ByteString;
import java.io.DataInputStream;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.openstreetmap.osmosis.osmbinary.Fileformat;
import org.openstreetmap.osmosis.osmbinary.Fileformat.Blob;

public class FileBlockSpliterator implements Spliterator<FileBlock> {

  private final DataInputStream input;

  public FileBlockSpliterator(DataInputStream input) {
    checkNotNull(input);
    this.input = input;
  }

  @Override
  public Spliterator<FileBlock> trySplit() {
    return null;
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return ORDERED | DISTINCT | NONNULL | IMMUTABLE;
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
      action.accept(new FileBlock(header.getType(), header.getIndexdata(), data(blob)));
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
