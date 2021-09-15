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
import com.baremaps.osm.cache.SimpleCache;
import com.baremaps.osm.cache.StoreCache;
import com.baremaps.store.LongFixedSizeDataDenseMap;
import com.baremaps.store.memory.OffHeapMemory;
import com.baremaps.store.type.CoordinateDataType;
import java.util.concurrent.TimeUnit;
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

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
public class CoordinateCacheBenchmark {

  private final long N = 1000000;

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
  public void store() throws CacheException {
    Cache<Long, Coordinate> cache =
        new StoreCache<>(
            new LongFixedSizeDataDenseMap<>(new CoordinateDataType(), new OffHeapMemory()));
    benchmark(cache, N);
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
