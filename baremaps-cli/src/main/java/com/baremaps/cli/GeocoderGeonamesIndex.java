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

package com.baremaps.cli;

import com.baremaps.baremaps.geonames.Geonames;
import com.baremaps.baremaps.geonames.GeonamesRecord;
import com.baremaps.core.blob.BlobStore;
import com.baremaps.geocoder.GeocoderLucene;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "geocoder-geonames-index", description = "Transform a text into a geocode point.")
public class GeocoderGeonamesIndex implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(GeocoderGeonamesIndex.class);

  @Mixin private Options options;

  @Option(
      names = {"--index"},
      paramLabel = "INDEX",
      description = "The path to the lucene index.",
      required = true)
  private Path indexPath;

  @Option(
      names = {"--geonames"},
      paramLabel = "GEONAMES",
      description = "The path to the geonames data.",
      required = true)
  private URI geonamesDataPath;

  @Override
  public Integer call() throws Exception {
    BlobStore blobStore = options.blobStore();
    GeocoderLucene geocoderLucene = new GeocoderLucene(indexPath);
    Geonames geonames = new Geonames();

    try (InputStream inputStream = blobStore.get(geonamesDataPath).getInputStream()) {
      logger.info("Parsing geonames file...");
      Stream<GeonamesRecord> geonamesRecordStream = geonames.parse(inputStream);
      logger.info("Indexing geonames into lucene...");
      geocoderLucene.indexGeonames(geonamesRecordStream);
      logger.info("Indexing finished.");
    }

    return 0;
  }
}
