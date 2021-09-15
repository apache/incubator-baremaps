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

import com.baremaps.store.FixedSizeDataList;
import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.memory.OnHeapMemory;
import com.baremaps.store.type.LongDataType;
import java.util.concurrent.TimeUnit;
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
public class StoreBenchmark {

  private final long N = 1 << 25;

  private void benchmark(FixedSizeDataList<Long> store, long n) {
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
    benchmark(new FixedSizeDataList<>(new LongDataType(), new OnHeapMemory()), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void offHeap() {
    benchmark(new FixedSizeDataList<>(new LongDataType(), new OffHeapMemory()), N);
  }

  /*
  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void file() throws IOException {
    benchmark(new FixedSizeDataList<>(new LongDataType(), new FileMemory()), N);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @Warmup(iterations = 2)
  @Measurement(iterations = 5)
  public void directory() throws IOException {
    benchmark(new FixedSizeDataList<>(new LongDataType(), new DirectoryMemory()), N);
  }
   */

  public static void main(String[] args) throws RunnerException {
    org.openjdk.jmh.runner.options.Options opt =
        new OptionsBuilder().include(StoreBenchmark.class.getSimpleName()).forks(1).build();
    new Runner(opt).run();
  }
}