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
import java.util.List;

public record DownloadUrl(String id, List<String> needs, String url, String path) implements Task {

  @Override
  public void run() {
    try (var inputStream = new URL(url).openStream()) {
      var downloadFile = Paths.get(path);
      Files.createDirectories(downloadFile.getParent());
      Files.copy(inputStream, downloadFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
