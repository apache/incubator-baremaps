package com.baremaps.osm.pbf;

import com.baremaps.osm.BlockEntityHandler;
import com.baremaps.osm.BlockHandler;
import com.baremaps.osm.BlockReader;
import com.baremaps.osm.DefaultEntityHandler;
import com.baremaps.osm.EntityHandler;
import com.baremaps.osm.EntityReader;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.stream.BatchSpliterator;
import com.baremaps.osm.stream.StreamException;
import com.baremaps.osm.stream.StreamProgress;
import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PbfReader implements EntityReader, BlockReader {

  private final InputStream inputStream;

  private final boolean parallel;

  private final long size;

  public PbfReader(InputStream inputStream) {
    this(inputStream, 0, false);
  }

  public PbfReader(InputStream inputStream, long size, boolean parallel) {
    this.inputStream = inputStream;
    this.size = size;
    this.parallel = parallel;
  }

  @Override
  public Stream<Entity> entities() {
    return blocks().flatMap(block -> {
      try {
        Stream.Builder<Entity> entities = Stream.builder();
        block.handle(new BlockEntityHandler(entities::add));
        return entities.build();
      } catch (Exception e) {
        throw new StreamException(e);
      }
    });
  }

  public Stream<Block> blocks() {
    Spliterator<Block> spliterator = new BlockSpliterator(inputStream);
    if (parallel) {
      spliterator = new BatchSpliterator<>(spliterator, 1);
    }
    Stream<Block> stream = StreamSupport.stream(spliterator, parallel);
    if (size > 0) {
      stream = stream.peek(new StreamProgress<>(size, block -> block.blob().size()));
    }
    return stream;
  }

}
