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



import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.apache.baremaps.testing.TestFiles;
import org.apache.baremaps.vectortile.expression.Expressions.Has;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.tasks.TransformEntityCollection.Operation;
import org.apache.baremaps.workflow.tasks.TransformEntityCollection.Recipe;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class CreateAndTransformEntityCollectionTest extends PostgresContainerTest {

  @Test
  @Tag("integration")
  void execute() throws Exception {
    var file = TestFiles.resolve("monaco/monaco.osm.pbf");
    var collection = Paths.get("entity_collection");
    var jdbcUrl = jdbcUrl();

    var createTask = new CreateEntityCollection(file, collection, 3857);
    createTask.execute(new WorkflowContext());

    var simplifyTask = new TransformEntityCollection(collection, jdbcUrl,
        new Recipe("building", new Has("landuse"), List.of("landuse"),
            Operation.union));
    simplifyTask.execute(new WorkflowContext());

    Files.delete(collection);
  }
}
