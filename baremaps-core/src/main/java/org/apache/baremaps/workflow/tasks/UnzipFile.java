/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.workflow.tasks;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipFile;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unzip a file.
 */
@JsonTypeName("UnzipFile")
public class UnzipFile implements Task {

  private static final Logger logger = LoggerFactory.getLogger(UnzipFile.class);

  private Path file;
  private Path directory;

  /**
   * Constructs an {@code UnzipFile}.
   */
  public UnzipFile() {

  }

  /**
   * Constructs an {@code UnzipFile}.
   *
   * @param file the file
   * @param directory the directory
   */
  public UnzipFile(Path file, Path directory) {
    this.file = file;
    this.directory = directory;
  }

  /**
   * Returns the file.
   *
   * @return the file
   */
  public Path getFile() {
    return file;
  }

  /**
   * Sets the file.
   *
   * @param file the file
   */
  public void setFile(Path file) {
    this.file = file;
  }

  /**
   * Returns the directory.
   *
   * @return the directory
   */
  public Path getDirectory() {
    return directory;
  }

  /**
   * Sets the directory.
   *
   * @param directory the directory
   */
  public void setDirectory(Path directory) {
    this.directory = directory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var filePath = file.toAbsolutePath();
    var directoryPath = directory.toAbsolutePath();

    try (var zipFile = new ZipFile(filePath.toFile())) {
      var entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        var ze = entries.nextElement();
        if (ze.isDirectory()) {
          continue;
        }

        var path = directoryPath.resolve(ze.getName());

        var file = path.toFile().getCanonicalFile();
        var directory = directoryPath.toFile().getCanonicalFile();
        if (!file.toPath().startsWith(directory.toPath())) {
          throw new IOException("Entry is outside of the target directory");
        }

        Files.createDirectories(path.getParent());
        Files.write(path, new byte[] {}, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);

        try (var input = new BufferedInputStream(zipFile.getInputStream(ze));
            var output = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {

          int nBytes;
          byte[] buffer = new byte[4096];
          while ((nBytes = input.read(buffer)) > 0) {
            output.write(buffer, 0, nBytes);
          }
        }
      }
    }
  }
}
