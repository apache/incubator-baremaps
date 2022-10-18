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
import org.apache.baremaps.cli.Options;
import org.apache.baremaps.geocoder.geonames.GeonamesGeocoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "index", description = "Index geonames data.")
public class Index implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Index.class);

  @Mixin
  private Options options;

  @Option(names = {"--index"}, paramLabel = "INDEX", description = "The path to the lucene index.",
      defaultValue = "geocoder_index")
  private Path index;

  @Option(names = {"--geonames"}, paramLabel = "GEONAMES",
      description = "The path of the geonames file.")
  private Path geonames;

  @Override
  public Integer call() throws Exception {
    logger.info("Creating the index");
    try (GeonamesGeocoder geocoder = new GeonamesGeocoder(index, geonames)) {
      geocoder.build();
    }
    logger.info("Index created successfully");
    return 0;
  }
}
