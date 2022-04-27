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

package com.baremaps.cli.geocoder;

import com.baremaps.geocoder.Geocoder;
import com.baremaps.geocoder.Request;
import com.baremaps.geocoder.Response;
import com.baremaps.geocoder.geonames.GeonamesGeocoder;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "search", description = "Search geonames data.")
public class Search implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(Search.class);

  @Option(
      names = {"--index-path"},
      paramLabel = "INDEX_PATH",
      description = "The path to the lucene index.",
      required = true)
  private Path indexPath;

  @Option(
      names = {"--search"},
      paramLabel = "SEARCH",
      description = "The terms to search in the index.",
      required = true)
  private String search;

  @Override
  public Integer call() throws Exception {
    Geocoder geocoder = new GeonamesGeocoder(indexPath, null);
    geocoder.open();

    Request request = new Request(search, 20);
    Response response = geocoder.search(request);

    System.out.println(response);

    return 0;
  }
}
