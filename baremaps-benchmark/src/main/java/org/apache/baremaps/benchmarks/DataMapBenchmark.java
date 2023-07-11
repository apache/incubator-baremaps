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



import java.util.concurrent.TimeUnit;
import org.apache.baremaps.database.collection.*;
import org.apache.baremaps.database.memory.OffHeapMemory;
import org.apache.baremaps.database.type.LongDataType;
import org.apache.baremaps.database.type.PairDataType;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class DataMapBenchmark {

  private static final long N = 1 << 24;

  private static void benchmark(DataMap<Long> map, long n) {
    for (long i = 0; i < n; i++) {
      map.put(i, i);
    }
    for (long i = 0; i < n; i++) {
      long v = map.get(i);
      if (v != i) {
        throw new RuntimeException("Invalid value");
      }
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void simpleDataMap() {
    benchmark(new Long2ObjectJaggedDataMap<>(new AppendOnlyBuffer<>(new LongDataType())), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void indexedDataMap() {
    benchmark(new Long2ObjectIndexedDataMap<>(new AppendOnlyBuffer<>(new LongDataType())), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void memoryAlignedDataMap() {
    benchmark(new Long2ObjectMemoryAlignedDataMap<>(new LongDataType(), new OffHeapMemory()), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void monotonicDataMap() {
    benchmark(new Long2ObjectMonotonicDataMap<>(new AppendOnlyBuffer<>(new LongDataType())), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void openHashDataMap() {
    benchmark(new Long2ObjectOpenHashDataMap<>(10_000_000L, 0.75f,
        () -> new Long2ObjectMemoryAlignedDataMap<>(
            new LongDataType(),
            new OffHeapMemory()),
        () -> new Long2ObjectMemoryAlignedDataMap<>(
            new LongDataType(),
            new OffHeapMemory())),
        N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void packedHashDataMap() {
    benchmark(new Long2LongPackedOpenHashDataMap(100_000_000L, 0.75f,
        () -> new Long2ObjectMemoryAlignedDataMap<>(
            new PairDataType<>(new LongDataType(), new LongDataType()),
            new OffHeapMemory())),
        N);
  }

  public static void main(String[] args) throws RunnerException {
    org.openjdk.jmh.runner.options.Options opt =
        new OptionsBuilder().include(DataMapBenchmark.class.getSimpleName()).forks(1).build();
    new Runner(opt).run();
  }
}
