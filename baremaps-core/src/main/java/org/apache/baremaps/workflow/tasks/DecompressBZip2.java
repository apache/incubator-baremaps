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

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecompressBZip2 implements Task {

  private static final Logger logger = LoggerFactory.getLogger(DecompressBZip2.class);

  private Path source;
  private Path target;

  public DecompressBZip2() {}

  public DecompressBZip2(Path source, Path target) {
    this.source = source;
    this.target = target;
  }

  public Path getSource() {
    return source;
  }

  public void setSource(Path source) {
    this.source = source;
  }

  public Path getTarget() {
    return target;
  }

  public void setTarget(Path target) {
    this.target = target;
  }

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var sourcePath = source.toAbsolutePath();
    try (var bufferedInputStream = new BufferedInputStream(Files.newInputStream(sourcePath));
        var compressedInputStream = new BZip2CompressorInputStream(bufferedInputStream)) {
      var targetPath = target.toAbsolutePath();
      if (!Files.exists(targetPath)) {
        Files.createDirectories(targetPath.getParent());
        Files.createFile(targetPath);
      }
      Files.copy(compressedInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
