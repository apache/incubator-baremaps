package com.baremaps.osm.progress;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class InputStreamProgress extends FilterInputStream {

  private final Consumer<Long> listener;

  private long position = 0;

  public InputStreamProgress(InputStream inputStream, Consumer<Long> listener) {
    super(inputStream);
    this.listener = listener;
  }

  @Override
  public int read() throws IOException {
    int b = super.read();
    if (b > 0) {
      position++;
      listener.accept(position);
    }
    return b;
  }

  @Override
  public int read(byte[] b) throws IOException {
    int n = super.read(b);
    if (n > 0) {
      position += n;
      listener.accept(position);
    }
    return n;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int n = super.read(b, off, len);
    if (n != -1) {
      position += n;
      listener.accept(position);
    }
    return n;
  }

  @Override
  public long skip(long n) throws IOException {
    long s = super.skip(n);
    if (s != -1) {
      position += s;
      listener.accept(position);
    }
    return s;
  }

}
