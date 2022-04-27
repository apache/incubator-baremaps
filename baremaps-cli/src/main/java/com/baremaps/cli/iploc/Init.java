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
import com.baremaps.iploc.data.IpLocStats;
import com.baremaps.iploc.database.SqliteUtils;
import com.baremaps.iploc.nic.NicFetcher;
import com.baremaps.iploc.nic.NicObject;
import com.baremaps.iploc.nic.NicParser;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

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

  @Option(
      names = {"--data-uri"},
      paramLabel = "DATA_URI",
      description = "The URI to the geonames data.")
  private URI dataURI;

  @Override
  public Integer call() throws Exception {
    try (Geocoder geocoder = new GeonamesGeocoder(indexPath, dataURI)) {

      if (!geocoder.indexExists()) {
        logger.info("Building the geocoder index");
        geocoder.build();
      }

      logger.info("Loading the geocoder index");
      geocoder.open();

      logger.info("Fetching NIC datasets");
      Stream<Path> nicPaths = new NicFetcher().fetch();

      logger.info("Generating NIC objects stream");
      Stream<NicObject> fetchNicObjectStream =
          nicPaths.flatMap(
              nicPath -> {
                try {
                  InputStream inputStream =
                      new GZIPInputStream(new BufferedInputStream(Files.newInputStream(nicPath)));
                  return NicParser.parse(inputStream)
                      .onClose(
                          () -> {
                            try {
                              inputStream.close();
                            } catch (IOException e) {
                              throw new UncheckedIOException(e);
                            }
                          });
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              });

      logger.info("Creating the Iploc database");
      String jdbcUrl = String.format("JDBC:sqlite:%s", databasePath.toString());
      try {
        SqliteUtils.executeResource(jdbcUrl, "iploc_init.sql");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      logger.info("Inserting the nic objects into the Iploc database");
      IpLoc ipLoc = new IpLoc(jdbcUrl, geocoder);
      ipLoc.insertNicObjects(fetchNicObjectStream);
      IpLocStats ipLocStats = ipLoc.getIplocStats();

      logger.info(
          String.format(
              "IpLoc stats\n"
                  + "-----------\n"
                  + "inetnumInsertedByAddress : %s\n"
                  + "inetnumInsertedByDescr : %s\n"
                  + "inetnumInsertedByCountry : %s\n"
                  + "inetnumInsertedByCountryCode : %s\n"
                  + "inetnumInsertedByGeoloc : %s\n"
                  + "inetnumNotInserted : %s\n",
              ipLocStats.getInsertedByAddressCount(),
              ipLocStats.getInsertedByDescrCount(),
              ipLocStats.getInsertedByCountryCount(),
              ipLocStats.getInsertedByCountryCodeCount(),
              ipLocStats.getInsertedByGeolocCount(),
              ipLocStats.getNotInsertedCount()));

      logger.info("IpLoc database created successfully");

      return 0;
    }
  }
}
