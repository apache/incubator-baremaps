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

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record UngzipFile(Path file, Path directory) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(UngzipFile.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var filePath = file.toAbsolutePath();
    var directoryPath = directory.toAbsolutePath();
    try (var zis = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(filePath)))) {
      var decompressed = directoryPath.resolve(filePath.getFileName().toString().substring(0,
          filePath.getFileName().toString().length() - 3));
      if (!Files.exists(decompressed)) {
        Files.createDirectories(decompressed.getParent());
        Files.createFile(decompressed);
      }
      Files.copy(zis, decompressed, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
