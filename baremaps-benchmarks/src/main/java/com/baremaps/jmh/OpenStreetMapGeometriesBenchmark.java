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

import com.baremaps.collection.DataStore;
import com.baremaps.collection.LongDataMap;
import com.baremaps.collection.LongDataOpenHashMap;
import com.baremaps.collection.memory.OnDiskDirectoryMemory;
import com.baremaps.collection.memory.OnHeapMemory;
import com.baremaps.collection.type.CoordinateDataType;
import com.baremaps.collection.type.LongListDataType;
import com.baremaps.collection.utils.FileUtils;
import com.baremaps.osm.function.EntityConsumerAdapter;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.pbf.PbfBlockReader;
import com.baremaps.osm.pbf.PbfEntityReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class OpenStreetMapGeometriesBenchmark {

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
  @Warmup(iterations = 0)
  @Measurement(iterations = 1)
  public void store() throws IOException {
    Path directory = Files.createTempDirectory(Paths.get("."), "baremaps_");
    LongDataMap<Coordinate> coordinates =
        new LongDataOpenHashMap<>(
            new DataStore<>(new CoordinateDataType(), new OnDiskDirectoryMemory(directory)));
    LongDataMap<List<Long>> references =
        new LongDataOpenHashMap<>(new DataStore<>(new LongListDataType(), new OnHeapMemory()));
    AtomicLong nodes = new AtomicLong(0);
    AtomicLong ways = new AtomicLong(0);
    AtomicLong relations = new AtomicLong(0);
    try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
      new PbfEntityReader(
              new PbfBlockReader().coordinates(coordinates).references(references).projection(4326))
          .stream(inputStream)
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
    FileUtils.deleteRecursively(directory);
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
