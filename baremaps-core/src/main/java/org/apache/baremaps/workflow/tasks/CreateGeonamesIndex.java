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

package org.apache.baremaps.workflow.tasks;

import org.apache.baremaps.geocoder.geonames.GeonamesGeocoder;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;

public record CreateGeonamesIndex(String geonamesDumpPath, String targetGeonamesIndexPath) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(CreateGeonamesIndex.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    logger.info("Generating geonames from {}", geonamesDumpPath);
    try (GeonamesGeocoder geocoder =
                 new GeonamesGeocoder(Path.of(targetGeonamesIndexPath), Path.of(geonamesDumpPath))) {
      if (!geocoder.indexExists()) {
        logger.info("Building the geocoder index");
        geocoder.build();
      }
    } catch(Exception e) {
      logger.error("Error while creating the geocoder index", e);
      return;
    }
    logger.info("Finished creating the Geocoder index {}", targetGeonamesIndexPath);
  }
}
