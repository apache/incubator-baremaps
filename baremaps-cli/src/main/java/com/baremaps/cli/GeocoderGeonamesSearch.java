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

import com.baremaps.geocoder.GeocoderLucene;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "geocoder-geonames-search", description = "Transform a text into a geocode point.")
public class GeocoderGeonamesSearch implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(GeocoderGeonamesSearch.class);

  @Option(
      names = {"--index"},
      paramLabel = "INDEX",
      description = "The path to the lucene index.",
      required = true)
  private Path indexPath;

  @Option(
      names = {"--search-value"},
      paramLabel = "SEARCH_VALUE",
      description = "The address to find in the index.",
      required = true)
  private String searchValue;

  @Override
  public Integer call() throws Exception {
    GeocoderLucene geocoderLucene = new GeocoderLucene(indexPath);
    logger.info("Searching into the lucene index for your search value...");
    geocoderLucene.search(searchValue);

    return 0;
  }
}
