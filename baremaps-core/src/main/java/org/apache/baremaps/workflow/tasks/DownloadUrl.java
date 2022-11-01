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

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record DownloadUrl(String url, String path, boolean replaceExisting) implements Task {

  public DownloadUrl(String url, String path) {
    this(url, path, false);
  }

  private static final Logger logger = LoggerFactory.getLogger(DownloadUrl.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    logger.info("Downloading {} to {}", url, path);

    var targetUrl = new URL(url);
    var targetPath = Paths.get(path);

    if (Files.exists(targetPath) && !replaceExisting) {
      var head = (HttpURLConnection) targetUrl.openConnection();
      head.setFollowRedirects(true);
      head.setRequestMethod("HEAD");
      var contentLength = head.getContentLengthLong();
      head.disconnect();
      if (Files.size(targetPath) == contentLength) {
        logger.info("Skipping download of {} to {}", url, path);
        return;
      }
    }

    var connection = (HttpURLConnection) targetUrl.openConnection();
    connection.setFollowRedirects(true);
    connection.setRequestMethod("GET");
    try (var inputStream = connection.getInputStream()) {
      var downloadFile = targetPath.toAbsolutePath();
      Files.createDirectories(downloadFile.getParent());
      Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
      logger.info("Finished downloading {} to {}", url, path);
    }
  }
}
