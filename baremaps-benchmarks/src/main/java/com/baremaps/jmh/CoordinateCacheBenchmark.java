package com.baremaps.jmh;

import com.baremaps.osm.cache.CacheException;
import com.baremaps.osm.cache.CoordinateCache;
import com.baremaps.osm.cache.InMemoryCoordinateCache;
import com.baremaps.osm.lmdb.LmdbCoordinateCache;
import com.baremaps.osm.rocksdb.RocksdbCoordinateCache;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.lmdbjava.Env;
import org.locationtech.jts.geom.Coordinate;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class CoordinateCacheBenchmark {

  private final long N = 100000;

  private void benchmark(CoordinateCache cache, long n) throws CacheException {
    for (long i = 0; i < n; i++) {
      cache.add(i, new Coordinate(i, i));
    }
    for (long i = 0; i < n; i++) {
      cache.get(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void inmemory() throws CacheException {
    benchmark(new InMemoryCoordinateCache(), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void lmdb() throws IOException, RocksDBException, CacheException {
    Path path = Files.createTempDirectory(Paths.get("."), "baremaps_").toAbsolutePath();
    Env<ByteBuffer> env = Env.create()
        .setMapSize(1_000_000_000_000L)
        .setMaxDbs(3)
        .open(path.toFile());
    CoordinateCache cache = new LmdbCoordinateCache(env);
    benchmark(cache, N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void rocksdb() throws IOException, RocksDBException, CacheException {
    Path path = Files.createTempDirectory(Paths.get("."), "baremaps_").toAbsolutePath();
    try (Options options = new Options().setCreateIfMissing(true);
        RocksDB db = RocksDB.open(options, path.toString())) {
      CoordinateCache cache = new RocksdbCoordinateCache(db);
      benchmark(cache, N);
    }
  }

  public static void main(String[] args) throws RunnerException {
    org.openjdk.jmh.runner.options.Options opt = new OptionsBuilder()
        .include(CoordinateCacheBenchmark.class.getSimpleName())
        .forks(1)
        .build();
    new Runner(opt).run();
  }

}
