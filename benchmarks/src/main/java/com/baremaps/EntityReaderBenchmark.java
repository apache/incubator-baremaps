package com.baremaps;

import com.baremaps.osm.ElementHandler;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
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
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.RunnerException;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class EntityReaderBenchmark {

  private Path path = Paths.get("./switzerland-latest.pbf");

  @Param({"false", "true"})
  public boolean parallel;

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
  @Warmup(iterations = 1)
  @Measurement(iterations = 1)
  public void entityStream() throws IOException {
    AtomicLong nodes = new AtomicLong(0);
    AtomicLong ways = new AtomicLong(0);
    AtomicLong relations = new AtomicLong(0);

    OpenStreetMap.entityStream(path, parallel).forEach(new ElementHandler() {
      @Override
      public void handle(Node node) {
        nodes.incrementAndGet();
      }

      @Override
      public void handle(Way way) {
        ways.incrementAndGet();
      }

      @Override
      public void handle(Relation relation) {
        relations.incrementAndGet();
      }
    });

    System.out.println();
    System.out.println("----------------------");
    System.out.println("nodes:     " + nodes.get());
    System.out.println("ways:      " + ways.get());
    System.out.println("relations: " + relations.get());
    System.out.println("----------------------");
    System.out.println();
  }

}
