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

package com.baremaps.workflow.tasks;

import com.baremaps.workflow.WorkflowException;
import java.nio.file.Paths;
import org.apache.sis.storage.FeatureSet;
import org.geotoolkit.data.shapefile.ShapefileFeatureStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ImportShapefile(String file, String database, Integer sourceSRID, Integer targetSRID)
    implements ImportFeatureTask {

  private static final Logger logger = LoggerFactory.getLogger(ImportShapefile.class);

  @Override
  public void run() {
    logger.info("Importing {} into {}", file, database);
    var uri = Paths.get(file).toUri();
    try (var shapefileStore = new ShapefileFeatureStore(uri)) {
      for (var resource : shapefileStore.components()) {
        if (resource instanceof FeatureSet featureSet) {
          saveFeatureSet(featureSet);
        }
      }
      logger.info("Finished importing {} into {}", file, database);
    } catch (Exception e) {
      logger.error("Failed importing {} into {}", file, database);
      throw new WorkflowException(e);
    }
  }
}
