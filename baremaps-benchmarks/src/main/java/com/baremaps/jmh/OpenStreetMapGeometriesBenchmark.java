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

import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.cache.CoordinateMapper;
import com.baremaps.osm.cache.LongListMapper;
import com.baremaps.osm.cache.LongMapper;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.function.EntityConsumerAdapter;
import com.baremaps.osm.lmdb.LmdbCache;
import com.baremaps.osm.rocksdb.RocksdbCache;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
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
    Path cacheDirectory = Files.createTempDirectory("baremaps_").toAbsolutePath();
    Env<ByteBuffer> env =
        Env.create().setMapSize(1_000_000_000_000L).setMaxDbs(3).open(cacheDirectory.toFile());
    Cache<Long, Coordinate> coordinateCache =
        new LmdbCache(
            env,
            env.openDbi("coordinate", DbiFlags.MDB_CREATE),
            new LongMapper(),
            new CoordinateMapper());
    Cache<Long, List<Long>> referenceCache =
        new LmdbCache(
            env,
            env.openDbi("reference", DbiFlags.MDB_CREATE),
            new LongMapper(),
            new LongListMapper());

    AtomicLong nodes = new AtomicLong(0);
    AtomicLong ways = new AtomicLong(0);
    AtomicLong relations = new AtomicLong(0);

    try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
      OpenStreetMap.streamPbfEntitiesWithGeometries(
              inputStream, coordinateCache, referenceCache, 4326)
          .forEach(
              new EntityConsumerAdapter() {
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
    Path cacheDirectory = Files.createTempDirectory("baremaps_").toAbsolutePath();

    ColumnFamilyDescriptor defaultCFD = new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions());
    ColumnFamilyDescriptor coordinatesCFD = new ColumnFamilyDescriptor("coordinates".getBytes(), new ColumnFamilyOptions());
    ColumnFamilyDescriptor referencesCFD = new ColumnFamilyDescriptor("references".getBytes(), new ColumnFamilyOptions());

    // Create the db and the column families
    try (org.rocksdb.Options options = new org.rocksdb.Options().setCreateIfMissing(true);
        RocksDB db = RocksDB.open(options, cacheDirectory.toString());
        ColumnFamilyHandle coordinatesCFH = db.createColumnFamily(coordinatesCFD);
        ColumnFamilyHandle referencesCFH = db.createColumnFamily(referencesCFD)) {
    }

    List<ColumnFamilyDescriptor> columnFamilyDescriptors = List.of(defaultCFD, coordinatesCFD, referencesCFD);
    List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
    try (DBOptions options = new DBOptions();
        RocksDB db = RocksDB.open(options, cacheDirectory.toString(), columnFamilyDescriptors, columnFamilyHandles)) {
      Cache<Long, Coordinate> coordinateCache =
          new RocksdbCache(db, columnFamilyHandles.get(1), new LongMapper(), new CoordinateMapper());
      Cache<Long, List<Long>> referenceCache =
          new RocksdbCache(db, columnFamilyHandles.get(2), new LongMapper(), new LongListMapper());

      AtomicLong nodes = new AtomicLong(0);
      AtomicLong ways = new AtomicLong(0);
      AtomicLong relations = new AtomicLong(0);

      try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
        OpenStreetMap.streamPbfEntitiesWithGeometries(
                inputStream, coordinateCache, referenceCache, 4326)
            .forEach(
                new EntityConsumerAdapter() {
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
    Options opt =
        new OptionsBuilder()
            .include(OpenStreetMapGeometriesBenchmark.class.getSimpleName())
            .forks(1)
            .build();
    new Runner(opt).run();
  }
}
