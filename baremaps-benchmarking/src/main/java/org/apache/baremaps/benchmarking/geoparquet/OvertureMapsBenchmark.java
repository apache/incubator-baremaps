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
import org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class OvertureMapsBenchmark {

  private static Path directory = Path.of("baremaps-benchmarking/data");

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(OvertureMapsBenchmark.class.getSimpleName())
        .forks(1)
        .build();
    new Runner(opt).run();
  }

  @Setup
  public void setup() throws IOException {
    if (!Files.exists(directory)) {
      try (var client = S3Client.builder()
          .region(Region.US_EAST_1)
          .credentialsProvider(new AnonymousAWSCredentialsProvider())
          .build()) {

        var listRequest = ListObjectsV2Request.builder()
            .bucket("overturemaps-us-west-2")
            .prefix("release/2024-09-18.0/theme=addresses/")
            .build();
        var objects = client.listObjectsV2(listRequest).contents();
        for (var object : objects) {
          var key = object.key();
          var name = key.substring(key.lastIndexOf("/") + 1);
          var file = directory.resolve(name);
          Files.createDirectories(file.getParent());
          if (!Files.exists(file)) {
            var getRequest = GetObjectRequest.builder()
                .bucket("overturemaps-us-west-2")
                .key(key)
                .build();
            client.getObject(getRequest, file);
          }
        }
      }
    }
  }

  @Benchmark
  public void read() {
    GeoParquetReader reader = new GeoParquetReader(directory.toUri());
    reader.read().count();
  }

  @Benchmark
  public void readParallel() {
    GeoParquetReader reader = new GeoParquetReader(directory.toUri());
    reader.readParallel().count();
  }


}
