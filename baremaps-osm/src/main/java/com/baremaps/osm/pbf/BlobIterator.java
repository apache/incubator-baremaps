package com.baremaps.osm.pbf;

import com.baremaps.osm.binary.Fileformat;
import com.baremaps.osm.domain.Blob;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class BlobIterator implements Iterator<Blob> {

  private final DataInputStream dis;

  private Blob next;

  public BlobIterator(InputStream input) {
    this.dis = new DataInputStream(input);
  }

  private Blob read() throws IOException {
    // Read blob header
    int headerSize = dis.readInt();
    byte[] headerBytes = new byte[headerSize];
    dis.readFully(headerBytes);
    Fileformat.BlobHeader header = Fileformat.BlobHeader.parseFrom(headerBytes);

    // Read blob data
    int dataSize = header.getDatasize();
    byte[] data = new byte[dataSize];
    dis.readFully(data);

    return new Blob(header, data, 8 + headerSize + dataSize);
  }

  @Override
  public boolean hasNext() {
    try {
      if (next == null) {
        next = read();
      }
      return true;
    } catch (IOException exception) {
      return false;
    }
  }

  @Override
  public Blob next() {
    try {
      if (next == null) {
        next = read();
      }
      Blob current = next;
      next = null;
      return current;
    } catch (IOException exception) {
      throw new NoSuchElementException();
    }
  }

}
