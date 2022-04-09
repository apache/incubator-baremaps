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

package com.baremaps.cli.pipeline;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.cli.Options;
import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "pipeline", description = "Execute a pipeline.")
public class Pipeline implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

  @Mixin private Options options;

  @Option(
      names = {"--config"},
      paramLabel = "CONFIG",
      description = "The pipeline configuration.",
      required = true)
  private URI config;

  @Override
  public Integer call() throws Exception {
    BlobStore blobStore = options.blobStore();
    Blob blob = blobStore.get(config);
    ObjectMapper mapper = new ObjectMapper();
    Config config = mapper.readValue(blob.getInputStream(), Config.class);
    Path directory = Files.createDirectories(Paths.get("pipeline"));
    Context context =
        new Context() {
          @Override
          public Path directory() {
            return directory;
          }

          @Override
          public BlobStore blobStore() {
            return blobStore;
          }
        };
    com.baremaps.pipeline.Pipeline pipeline = new com.baremaps.pipeline.Pipeline(context, config);
    pipeline.execute();
    return 0;
  }
}
