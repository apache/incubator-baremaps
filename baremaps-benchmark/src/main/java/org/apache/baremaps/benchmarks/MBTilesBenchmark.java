/*
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

package org.apache.baremaps.benchmarks;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStoreException;
import org.apache.baremaps.tilestore.mbtiles.MBTiles;
import org.apache.baremaps.workflow.tasks.ExportVectorTiles;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1, warmups = 1)
public class MBTilesBenchmark {

  public Random random = new Random(0);

  @Param({"10", "100", "1000"})
  public int iterations;

  private MBTiles mbTiles;

  @Setup
  public void setup() throws IOException, TileStoreException {
    var sqliteFile = File.createTempFile("baremaps", ".sqlite");
    sqliteFile.deleteOnExit();
    var sqliteDataSource = ExportVectorTiles.createDataSource(sqliteFile.toPath());
    mbTiles = new MBTiles(sqliteDataSource);
    mbTiles.initializeDatabase();
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  public void writeMBTiles(MBTilesBenchmark benchmark) throws TileStoreException {
    for (int i = 0; i < benchmark.iterations; i++) {
      var bytes = new byte[1 << 16];
      random.nextBytes(bytes);
      mbTiles.put(new TileCoord(0, 0, i), ByteBuffer.wrap(bytes));
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  public void writeMBTilesBatch(MBTilesBenchmark benchmark) throws TileStoreException {
    var coords = new ArrayList<TileCoord>();
    var buffers = new ArrayList<ByteBuffer>();
    for (int i = 0; i < benchmark.iterations; i++) {
      var bytes = new byte[1 << 16];
      random.nextBytes(bytes);
      coords.add(new TileCoord(0, 0, i));
      buffers.add(ByteBuffer.wrap(bytes));
      if (coords.size() == 100) {
        random.nextBytes(bytes);
        mbTiles.put(coords, buffers);
        coords.clear();
        buffers.clear();
      }
    }
    mbTiles.put(coords, buffers);
    coords.clear();
    buffers.clear();
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder().include(MBTilesBenchmark.class.getSimpleName()).forks(1).build();
    new Runner(opt).run();
  }


}
