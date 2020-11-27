package com.baremaps.osm.pbf;

import com.baremaps.osm.EntityReader;
import com.baremaps.osm.binary.Osmformat;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.stream.BatchSpliterator;
import com.baremaps.osm.stream.StreamException;
import com.baremaps.osm.stream.StreamProgress;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.DataFormatException;

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
    return StreamSupport.stream(new ForkJoinBlobSpliterator(inputStream), parallel).flatMap(s -> s);
  }

}
