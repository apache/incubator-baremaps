package com.baremaps.osm.pbf;

import com.baremaps.osm.EntityReader;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.stream.AsyncSpliterator;
import com.baremaps.osm.stream.ParallelSpliterator;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PbfEntityReader implements EntityReader {

  private final InputStream inputStream;

  private final boolean parallel;

  private final boolean async;

  public PbfEntityReader(InputStream inputStream) {
    this(inputStream, false, false);
  }

  public PbfEntityReader(InputStream inputStream, boolean parallel, boolean async) {
    this.inputStream = inputStream;
    this.parallel = parallel;
    this.async = async;
  }

  @Override
  public Stream<Entity> stream() {
    return blobStream().flatMap(s -> s);
  }

  public Stream<Stream<Entity>> blobStream() {
    return async ? asyncStream() : syncStream();
  }

  private Stream<Stream<Entity>> syncStream() {
    Spliterator<Blob> spliterator = new BlobSpliterator(inputStream);
    if (parallel) {
      spliterator = new ParallelSpliterator<>(spliterator, 1);
    }
    return StreamSupport.stream(spliterator, parallel)
        .map(Blob::readFileBlock)
        .map(FileBlock::streamEntities);
  }

  private Stream<Stream<Entity>> asyncStream() {
    Function<Blob, Stream<Entity>> operation = blob -> blob.readFileBlock().streamEntities();
    Spliterator<Stream<Entity>> spliterator = new AsyncSpliterator<>(new BlobSpliterator(inputStream), operation);
    if (parallel) {
      spliterator = new ParallelSpliterator<>(spliterator, 1);
    }
    return StreamSupport.stream(spliterator, parallel);
  }

}
