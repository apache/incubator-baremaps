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
import org.apache.baremaps.openstreetmap.utils.ProjectionTransformer;
import org.apache.baremaps.storage.TableGeometryDecorator;
import org.apache.baremaps.storage.postgres.PostgresStore;
import org.apache.baremaps.storage.shapefile.ShapefileTable;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ImportShapefile(Path file, String database, Integer sourceSRID, Integer targetSRID)
    implements
      Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportShapefile.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var path = file.toAbsolutePath();
    try {
      var featureSet = new ShapefileTable(path);
      var dataSource = context.getDataSource(database);
      var postgresDatabase = new PostgresStore(dataSource);
      postgresDatabase.add(new TableGeometryDecorator(
          featureSet, new ProjectionTransformer(sourceSRID, targetSRID)));
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
