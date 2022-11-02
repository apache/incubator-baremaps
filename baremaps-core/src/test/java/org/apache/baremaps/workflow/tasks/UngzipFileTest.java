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
import org.apache.baremaps.collection.utils.FileUtils;
import org.apache.baremaps.testing.TestFiles;
import org.apache.baremaps.workflow.WorkflowContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class UngzipFileTest {

  @Test
  @Tag("integration")
  void run() throws Exception {
    var gzip = TestFiles.resolve("ripe/sample.txt.gz");
    var directory = Files.createTempDirectory("tmp_");
    var task = new UngzipFile(gzip.toString(), directory.toString());
    task.execute(new WorkflowContext());
    FileUtils.deleteRecursively(directory);
  }
}
