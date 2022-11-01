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

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import org.apache.baremaps.collection.utils.FileUtils;
import org.apache.baremaps.workflow.WorkflowContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class DownloadUrlTest {

  @Test
  @Tag("integration")
  void execute() throws Exception {
    var file = File.createTempFile("test", ".tmp");
    file.deleteOnExit();
    var task = new DownloadUrl("https://raw.githubusercontent.com/baremaps/baremaps/main/README.md",
        file.getAbsolutePath());
    task.execute(new WorkflowContext());
    assertTrue(Files.readString(file.toPath()).contains("Baremaps"));
  }

  @Test
  @Tag("integration")
  void executeFileThatDoesntExist() throws Exception {
    var directory = Files.createTempDirectory("tmp_");
    var file = directory.resolve("README.md");
    var task = new DownloadUrl("https://raw.githubusercontent.com/baremaps/baremaps/main/README.md",
        file.toAbsolutePath().toString());
    task.execute(new WorkflowContext());
    assertTrue(Files.readString(file).contains("Baremaps"));
    FileUtils.deleteRecursively(directory);
  }
}
