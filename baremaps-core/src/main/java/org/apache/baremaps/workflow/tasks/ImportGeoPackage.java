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

import java.nio.file.Path;
import org.apache.baremaps.feature.ReadableFeatureSet;
import org.apache.baremaps.openstreetmap.utils.ProjectionTransformer;
import org.apache.baremaps.storage.FeatureSetProjectionTransform;
import org.apache.baremaps.storage.geopackage.GeoPackageDatabase;
import org.apache.baremaps.storage.postgres.PostgresDatabase;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ImportGeoPackage(Path file, String database, Integer sourceSRID, Integer targetSRID)
    implements
      Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportGeoPackage.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var path = file.toAbsolutePath();
    try (var geoPackageStore = new GeoPackageDatabase(path)) {
      var dataSource = context.getDataSource(database);
      var postgresDatabase = new PostgresDatabase(dataSource);
      for (var resource : geoPackageStore.read().toList()) {
        if (resource instanceof ReadableFeatureSet featureSet) {
          var transformer = new ProjectionTransformer(sourceSRID, targetSRID);
          var transformedFeatureSet = new FeatureSetProjectionTransform(featureSet, transformer);
          postgresDatabase.write(transformedFeatureSet);
        }
      }
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
