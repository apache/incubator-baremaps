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

import org.apache.baremaps.openstreetmap.utils.ProjectionTransformer;
import org.apache.baremaps.storage.FeatureSetProjectionTransform;
import org.apache.baremaps.storage.postgres.PostgresDatabase;
import org.apache.baremaps.storage.shapefile.ShapefileFeatureSet;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ImportShapefile(String file, String database, Integer sourceSRID, Integer targetSRID)
  implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportShapefile.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    logger.info("Importing {} into {}", file, database);
    var path = Paths.get(file);
    try (var featureSet = new ShapefileFeatureSet(path)) {
      var dataSource = context.getDataSource(database);
      var postgresDatabase = new PostgresDatabase(dataSource);
      postgresDatabase.add(new FeatureSetProjectionTransform(
        featureSet, new ProjectionTransformer(sourceSRID, targetSRID)));
      logger.info("Finished importing {} into {}", file, database);
    } catch (Exception e) {
      logger.error("Failed importing {} into {}", file, database);
      throw new WorkflowException(e);
    }
  }
}
