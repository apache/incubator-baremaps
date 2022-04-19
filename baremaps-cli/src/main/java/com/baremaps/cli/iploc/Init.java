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

import com.baremaps.geocoder.Geocoder;
import com.baremaps.geocoder.geonames.GeonamesGeocoder;
import com.baremaps.iploc.IpLoc;
import com.baremaps.iploc.database.SqliteUtils;
import com.baremaps.iploc.nic.NicFetcher;
import com.baremaps.iploc.nic.NicObject;
import com.baremaps.iploc.nic.NicParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Command(name = "init", description = "Generate the IpLoc database.")
public class Init implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Init.class);

  @Option(
      names = {"--index-path"},
      paramLabel = "INDEX_PATH",
      description = "The path to the geocoder Lucene index.",
      defaultValue = "geocoder_index")
  private Path indexPath;

  @Option(
      names = {"--database-path"},
      paramLabel = "DATABASE_PATH",
      description = "The path to the output SQLite database.",
      defaultValue = "iploc.db")
  private Path databasePath;

  @Override
  public Integer call() throws IOException, SQLException, URISyntaxException {

    String jdbcUrl = String.format("JDBC:sqlite:%s", databasePath.toString());

    CompletableFuture<Geocoder> loadGeocoderIndex
            = CompletableFuture.supplyAsync(() -> {
      try {
        logger.info("Loading the geocoder index");
        Geocoder geocoder = new GeonamesGeocoder(indexPath);
        geocoder.open();
        return geocoder;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    CompletableFuture<Stream<NicObject>> fetchNicObjectStream
            = CompletableFuture.supplyAsync(() -> {
      logger.info("Fetching NIC datasets");
      Stream<Path> nicPathsStream = null;
      try {
        nicPathsStream = new NicFetcher().fetch();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      //nicPathsStream = Stream.of(Paths.get("baremaps-iploc/src/test/resources/simple_nic_sample.txt"));

      logger.info("Generating NIC objects stream");
      return nicPathsStream.flatMap(
                      nicPath -> {
                        try {
                          InputStream inputStream = new BufferedInputStream(Files.newInputStream(nicPath));
                          return NicParser.parse(inputStream).onClose(() -> {
                            try {
                              inputStream.close();
                            } catch (IOException e) {
                              e.printStackTrace();
                            }
                          });
                        } catch (IOException e) {
                          e.printStackTrace();
                        }
                        return Stream.empty();
                      });
    });

    CompletableFuture<Void> initTheDatabase
            = CompletableFuture.supplyAsync(() -> {
      logger.info("Creating the Iploc database");
      try {
        SqliteUtils.executeResource(jdbcUrl, "iploc_init.sql");
        return null;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    CompletableFuture.allOf(loadGeocoderIndex, fetchNicObjectStream, initTheDatabase).join();

    logger.info("Inserting the nic objects into the Iploc database");
    IpLoc ipLoc = new IpLoc(jdbcUrl, loadGeocoderIndex.join());
    ipLoc.insertNicObjects(fetchNicObjectStream.join());

    logger.info("IpLoc database created successfully");

    return 0;
  }
}
