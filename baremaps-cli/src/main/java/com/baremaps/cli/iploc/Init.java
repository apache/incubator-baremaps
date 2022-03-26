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

package com.baremaps.cli.iploc;

import com.baremaps.cli.Options;
import com.baremaps.core.blob.Blob;
import com.baremaps.core.blob.BlobStore;
import com.baremaps.core.blob.BlobStoreException;
import com.baremaps.geocoder.Geocoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.baremaps.server.utils.DefaultObjectMapper.defaultObjectMapper;

@Command(name = "init", description = "Init the Iploc database.")
public class Init implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Init.class);

  @Mixin private Options options;

  @Option(
      names = {"--geocoder-index-path"},
      paramLabel = "GEOCODER_INDEX_PATH",
      description = "The path to the geocoder Lucene index.")
  private Path geocoderIndexPath;

  @Option(
          names = {"--database-path"},
          paramLabel = "DATABASE_PATH",
          description = "The path to the target database.",
          required = true)
  private Path databasePath;

  @Override
  public Integer call() throws BlobStoreException, IOException {

    logger.info("Loading the geocoder index");

    logger.info("Fetching NIC datasets");

    logger.info("Generating NIC objects stream");

    logger.info("Generation Iploc database");

    return 0;
  }
}
