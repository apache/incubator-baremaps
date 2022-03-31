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
<<<<<<< Updated upstream
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
=======
import com.baremaps.core.blob.BlobStoreException;
import com.baremaps.geocoder.Geocoder;
import com.baremaps.geocoder.geonames.GeonamesGeocoder;
import com.baremaps.iploc.IpLoc;
import com.baremaps.iploc.nic.NicFetcher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import com.baremaps.iploc.nic.NicObject;
import com.baremaps.iploc.nic.NicParser;
import com.baremaps.iploc.sqlite.SqliteUtils;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
>>>>>>> Stashed changes

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
  public Integer call() throws BlobStoreException, IOException, SQLException, URISyntaxException {

    logger.info("Loading the geocoder index");
    Path path = Files.createTempDirectory(Paths.get("."), "geocoder_");
    URI data = Resources.getResource("geocoder_sample.txt").toURI();
    Geocoder geocoder = new GeonamesGeocoder(path, data);
    geocoder.build();


    logger.info("Fetching NIC datasets");
    Stream<Path> nicPathsStream = new NicFetcher().fetch();

    logger.info("Generating NIC objects stream");
    Stream<NicObject> nicObjectStream = nicPathsStream.flatMap(nicPath -> {
      try {
        return NicParser.parse(new BufferedInputStream(Files.newInputStream(nicPath)));
      } catch (IOException e) {
        e.printStackTrace();
      }
      return Stream.empty();
    });

    logger.info("Creating the Iploc database");
    String databaseUrl = "JDBC:sqlite:test.db";
    SqliteUtils.executeResource(databaseUrl, "iploc_init.sql");

    logger.info("Inserting the nic objects into the Iploc database");
    IpLoc ipLoc = new IpLoc(databaseUrl, geocoder);
    ipLoc.insertNicObjects(nicObjectStream);

    return 0;
  }
}
