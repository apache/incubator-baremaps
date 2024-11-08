/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.benchmarking.geoparquet;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.apache.baremaps.geoparquet.GeoParquetReader;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class SmallFileBenchmark {

  private Path source = Path.of("baremaps-testing/data/samples/example.parquet").toAbsolutePath();
  private Path directory = Path.of("baremaps-benchmarking/data/small").toAbsolutePath();

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(SmallFileBenchmark.class.getSimpleName())
        .forks(1)
        .build();
    new Runner(opt).run();
  }

  @Setup
  public void setup() throws IOException {
    if (!Files.exists(directory)) {
      for (int i = 0; i < 1000; i++) {
        Path target = directory.resolve(i + ".parquet");
        Files.createDirectories(target.getParent());
        Files.copy(source, target);
      }
    }
  }

  @SuppressWarnings({"squid:S1481", "squid:S2201"})
  @Benchmark
  public void read() {
    var path = new org.apache.hadoop.fs.Path("baremaps-benchmarking/data/small/*.parquet");
    GeoParquetReader reader = new GeoParquetReader(path);
    reader.read().count();
  }

  @SuppressWarnings({"squid:S1481", "squid:S2201"})
  @Benchmark
  public void readParallel() {
    var path = new org.apache.hadoop.fs.Path("baremaps-benchmarking/data/small/*.parquet");
    GeoParquetReader reader =
        new GeoParquetReader(path);
    reader.readParallel().count();
  }
}
