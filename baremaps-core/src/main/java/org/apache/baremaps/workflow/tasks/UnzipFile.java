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

import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record UnzipFile(String file, String directory) implements Task {

  private static final long THRESHOLD_ENTRIES = 10000;
  private static final long THRESHOLD_SIZE = 10l << 30;
  private static final double THRESHOLD_RATIO = 100;

  private static final Logger logger = LoggerFactory.getLogger(UnzipFile.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    logger.info("Unzipping {} to {}", file, directory);

    var filePath = Paths.get(file).toAbsolutePath();
    var directoryPath = Paths.get(directory).toAbsolutePath();

    try(var zipFile = new ZipFile(filePath.toFile())) {
      var entries = zipFile.entries();
      long totalSizeArchive = 0;
      long totalEntryArchive = 0;

      while (entries.hasMoreElements()) {
        var ze = entries.nextElement();
        var file = directoryPath.resolve(ze.getName());

        if (!file.toFile().getCanonicalPath().startsWith(directoryPath.toFile().getCanonicalPath())) {
          throw new IOException("Entry is outside of the target directory");
        }

        Files.createDirectories(file.getParent());
        Files.write(file, new byte[]{}, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        try(var input = new BufferedInputStream(zipFile.getInputStream(ze));
        var output = new BufferedOutputStream(new FileOutputStream(file.toFile()))) {

          totalEntryArchive++;

          int nBytes = -1;
          byte[] buffer = new byte[4096];
          long totalSizeEntry = 0;

          while ((nBytes = input.read(buffer)) > 0) {
            output.write(buffer, 0, nBytes);
            totalSizeEntry += nBytes;
            totalSizeArchive += nBytes;

            double compressionRatio = (double) totalSizeEntry / (double) ze.getCompressedSize();
            if (compressionRatio > THRESHOLD_RATIO) {
              throw new WorkflowException("Archive compression ratio is too high");
            }
          }

          if (totalSizeArchive > THRESHOLD_SIZE) {
            throw new IOException("Archive is too large");
          }

          if (totalEntryArchive > THRESHOLD_ENTRIES) {
            throw new IOException("Archive contains too many entries");
          }
        }
      }
    }
  }
}
