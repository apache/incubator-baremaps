/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.jmh;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CacheException;
import com.baremaps.osm.cache.CoordinateMapper;
import com.baremaps.osm.cache.LongMapper;
import com.baremaps.osm.cache.SimpleCache;
import com.baremaps.osm.lmdb.LmdbCache;
import com.baremaps.osm.rocksdb.RocksdbCache;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.lmdbjava.DbiFlags;
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
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class CoordinateCacheBenchmark {

  private final long N = 100000;

  private void benchmark(Cache<Long, Coordinate> cache, long n) throws CacheException {
    for (long i = 0; i < n; i++) {
      cache.put(i, new Coordinate(i, i));
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
    benchmark(new SimpleCache(), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void lmdb() throws IOException, RocksDBException, CacheException {
    Path path = Files.createTempDirectory("baremaps_").toAbsolutePath();
    Env<ByteBuffer> env =
        Env.create().setMapSize(1_000_000_000_000L).setMaxDbs(3).open(path.toFile());
    Cache<Long, Coordinate> cache =
        new LmdbCache(
            env,
            env.openDbi("coordinate", DbiFlags.MDB_CREATE),
            new LongMapper(),
            new CoordinateMapper());
    benchmark(cache, N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void rocksdb() throws IOException, RocksDBException, CacheException {
    Path path = Files.createTempDirectory("baremaps_").toAbsolutePath();

    ColumnFamilyDescriptor defaultCFD = new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions());
    ColumnFamilyDescriptor columnCFD = new ColumnFamilyDescriptor("coordinates".getBytes(), new ColumnFamilyOptions());

    // Create the db and the column family
    try (Options options = new Options().setCreateIfMissing(true);
        RocksDB db = RocksDB.open(options, path.toString());
        ColumnFamilyHandle ignored = db.createColumnFamily(columnCFD)) {
    }

    List<ColumnFamilyDescriptor> columnFamilyDescriptors = List.of(defaultCFD, columnCFD);
    List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
    try (DBOptions options = new DBOptions();
        RocksDB db = RocksDB.open(options, path.toString(), columnFamilyDescriptors, columnFamilyHandles)) {
      Cache<Long, Coordinate> cache =
          new RocksdbCache(db, columnFamilyHandles.get(1), new LongMapper(), new CoordinateMapper());
      benchmark(cache, N);
    }
  }

  public static void main(String[] args) throws RunnerException {
    org.openjdk.jmh.runner.options.Options opt =
        new OptionsBuilder()
            .include(CoordinateCacheBenchmark.class.getSimpleName())
            .forks(1)
            .build();
    new Runner(opt).run();
  }
}
