package com.baremaps.jmh;

import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.ElementConsumer;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class EntityReaderBenchmark {

  private final Path path = Paths.get("./switzerland-latest.pbf");

  @Setup
  public void setup() throws IOException {
    URL url = new URL("http://download.geofabrik.de/europe/switzerland-latest.osm.pbf");
    if (!Files.exists(path)) {
      try (InputStream inputStream = url.openStream()) {
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void entityStream() throws IOException {
    AtomicLong nodes = new AtomicLong(0);
    AtomicLong ways = new AtomicLong(0);
    AtomicLong relations = new AtomicLong(0);

    try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
      OpenStreetMap.streamPbfEntities(inputStream).forEach(new ElementConsumer() {
        @Override
        public void match(Node node) {
          nodes.incrementAndGet();
        }

        @Override
        public void match(Way way) {
          ways.incrementAndGet();
        }

        @Override
        public void match(Relation relation) {
          relations.incrementAndGet();
        }
      });
    }
  }

}
