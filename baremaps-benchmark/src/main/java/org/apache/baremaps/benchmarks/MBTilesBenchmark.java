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

package org.apache.baremaps.benchmarks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStoreException;
import org.apache.baremaps.tilestore.mbtiles.MBTilesStore;
import org.apache.baremaps.utils.SqliteUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1, warmups = 1)
public class MBTilesBenchmark {

  public static SecureRandom random = new SecureRandom();

  @Param({"10", "100", "1000"})
  public int iterations;

  private Path file;

  private MBTilesStore mbTilesStore;

  @Setup
  public void setup() throws IOException, TileStoreException {
    file = Files.createTempFile(Paths.get("."), "baremaps", ".mbtiles");
    mbTilesStore = new MBTilesStore(SqliteUtils.createDataSource(file, false));
    mbTilesStore.initializeDatabase();
  }

  @TearDown
  public void tearDown() throws IOException, TileStoreException {
    Files.delete(file);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  public void writeMBTiles(MBTilesBenchmark benchmark) throws TileStoreException {
    for (int i = 0; i < benchmark.iterations; i++) {
      var bytes = new byte[1 << 16];
      random.nextBytes(bytes);
      mbTilesStore.write(new TileCoord(0, 0, i), ByteBuffer.wrap(bytes));
    }
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder().include(MBTilesBenchmark.class.getSimpleName()).forks(1).build();
    new Runner(opt).run();
  }
}
