/*
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

package org.apache.baremaps.cli.geocoder;



import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.apache.baremaps.geocoder.Request;
import org.apache.baremaps.geocoder.geonames.GeonamesGeocoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "search", description = "Search geonames index.")
public class Search implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Search.class);

  @Option(names = {"--index"}, paramLabel = "INDEX", description = "The path to the lucene index.",
      required = true)
  private Path indexPath;

  @Option(names = {"--search"}, paramLabel = "SEARCH",
      description = "The terms to search in the index.", required = true)
  private String search;

  @Override
  public Integer call() throws Exception {
    var geocoder = new GeonamesGeocoder(indexPath, null);
    geocoder.open();
    var request = new Request(search, 20);
    var response = geocoder.search(request);
    logger.info("{}", response);
    return 0;
  }
}
