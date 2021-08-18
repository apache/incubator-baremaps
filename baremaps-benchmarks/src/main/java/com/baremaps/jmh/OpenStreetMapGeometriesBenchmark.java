package com.baremaps.jmh;

import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.cache.CoordinateCache;
import com.baremaps.osm.cache.ReferenceCache;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.handler.EntityConsumerAdapter;
import com.baremaps.osm.lmdb.LmdbCoordinateCache;
import com.baremaps.osm.lmdb.LmdbReferencesCache;
import com.baremaps.osm.rocksdb.RocksdbCoordinateCache;
import com.baremaps.osm.rocksdb.RocksdbReferencesCache;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.lmdbjava.Env;
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
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class OpenStreetMapGeometriesBenchmark {

  private final Path path = Paths.get("./liechtenstein-latest.pbf");

  @Setup
  public void setup() throws IOException {
    URL url = new URL("http://download.geofabrik.de/europe/liechtenstein-latest.osm.pbf");
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
  public void lmdb() throws IOException {
    Path cacheDirectory = Files.createTempDirectory(Paths.get("."), "baremaps_").toAbsolutePath();
    Env<ByteBuffer> env = Env.create()
        .setMapSize(1_000_000_000_000L)
        .setMaxDbs(3)
        .open(cacheDirectory.toFile());
    CoordinateCache coordinateCache = new LmdbCoordinateCache(env);
    ReferenceCache referenceCache = new LmdbReferencesCache(env);

    AtomicLong nodes = new AtomicLong(0);
    AtomicLong ways = new AtomicLong(0);
    AtomicLong relations = new AtomicLong(0);

    try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
      OpenStreetMap.streamPbfEntitiesWithGeometries(inputStream, coordinateCache, referenceCache, 4326)
          .forEach(new EntityConsumerAdapter() {
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

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void rocksdb() throws IOException, RocksDBException {
    Path coordinatesDirectory = Files.createTempDirectory(Paths.get("."), "baremaps_").toAbsolutePath();
    Path referenceDirectory = Files.createTempDirectory(Paths.get("."), "baremaps_").toAbsolutePath();

    try (org.rocksdb.Options options = new org.rocksdb.Options().setCreateIfMissing(true);
        RocksDB coordinatesDB = RocksDB.open(options, coordinatesDirectory.toString());
        RocksDB referenceDB = RocksDB.open(options, referenceDirectory.toString())) {
      CoordinateCache coordinateCache = new RocksdbCoordinateCache(coordinatesDB);
      ReferenceCache referenceCache = new RocksdbReferencesCache(referenceDB);

      AtomicLong nodes = new AtomicLong(0);
      AtomicLong ways = new AtomicLong(0);
      AtomicLong relations = new AtomicLong(0);

      try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
        OpenStreetMap.streamPbfEntitiesWithGeometries(inputStream, coordinateCache, referenceCache, 4326)
            .forEach(new EntityConsumerAdapter() {
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

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(OpenStreetMapGeometriesBenchmark.class.getSimpleName())
        .forks(1)
        .build();
    new Runner(opt).run();
  }

}
