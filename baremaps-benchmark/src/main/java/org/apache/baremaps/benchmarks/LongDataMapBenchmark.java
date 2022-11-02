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



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.apache.baremaps.collection.AlignedDataList;
import org.apache.baremaps.collection.memory.OffHeapMemory;
import org.apache.baremaps.collection.memory.OnDiskDirectoryMemory;
import org.apache.baremaps.collection.memory.OnHeapMemory;
import org.apache.baremaps.collection.type.LongDataType;
import org.apache.baremaps.collection.utils.FileUtils;
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

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class LongDataMapBenchmark {

  private static final long N = 1 << 25;

  private void benchmark(AlignedDataList<Long> store, long n) {
    for (long i = 0; i < n; i++) {
      store.add(i);
    }
    for (long i = 0; i < n; i++) {
      store.get(i);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void onHeap() {
    benchmark(new AlignedDataList<>(new LongDataType(), new OnHeapMemory()), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void offHeap() {
    benchmark(new AlignedDataList<>(new LongDataType(), new OffHeapMemory()), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void onDisk() throws IOException {
    Path directory = Files.createTempDirectory(Paths.get("."), "baremaps_");
    benchmark(new AlignedDataList<>(new LongDataType(), new OnDiskDirectoryMemory(directory)), N);
    FileUtils.deleteRecursively(directory);
  }

  public static void main(String[] args) throws RunnerException {
    org.openjdk.jmh.runner.options.Options opt =
        new OptionsBuilder().include(LongDataMapBenchmark.class.getSimpleName()).forks(1).build();
    new Runner(opt).run();
  }
}
