package com.baremaps.osm.pbf;

import com.baremaps.osm.EntityReader;
import com.baremaps.osm.domain.Entity;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PbfEntityReader implements EntityReader {

  private final InputStream inputStream;

  private final boolean parallel;

  public PbfEntityReader(InputStream inputStream) {
    this(inputStream, false);
  }

  public PbfEntityReader(InputStream inputStream, boolean parallel) {
    this.inputStream = inputStream;
    this.parallel = parallel;
  }

  @Override
  public Stream<Entity> stream() throws IOException {
    return StreamSupport.stream(new ParallelBlobSpliterator(inputStream), parallel).flatMap(s -> s);
  }

}
