/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.workflow.tasks;

import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record DownloadUrl(String url, String path) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(DownloadUrl.class);

  @Override
  public void run() {
    logger.info("Downloading {} to {}", url, path);
    try (var inputStream = new URL(url).openStream()) {
      var downloadFile = Paths.get(path).toAbsolutePath();
      Files.createDirectories(downloadFile.getParent());
      Files.copy(inputStream, downloadFile, StandardCopyOption.REPLACE_EXISTING);
      logger.info("Finished downloading {} to {}", url, path);
    } catch (Exception e) {
      logger.error("Failed downloading {} to {}", url, path);
      throw new WorkflowException(e);
    }
  }
}
