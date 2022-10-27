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



import org.apache.baremaps.testing.PostgresContainerTest;
import org.apache.baremaps.testing.TestFiles;
import org.apache.baremaps.workflow.WorkflowContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ImportOpenStreetMapTest extends PostgresContainerTest {

  @Test
  @Tag("integration")
  void execute() throws Exception {
    var task =
        new ImportOpenStreetMap(TestFiles.resolve("data.osm.pbf").toString(), jdbcUrl(), 3857);
    task.execute(new WorkflowContext());
  }
}
