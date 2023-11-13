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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads a file from a URL.
 */
@JsonTypeName("DownloadUrl")
public class DownloadUrl implements Task {

  private static final Logger logger = LoggerFactory.getLogger(DownloadUrl.class);

  private static final String PROTOCOL_FTP = "ftp";

  private static final String PROTOCOL_HTTP = "http";

  private static final String PROTOCOL_HTTPS = "https";

  private String url;

  private Path path;

  private Boolean replaceExisting;

  /**
   * Constructs an {@code DownloadUrl}.
   */
  public DownloadUrl() {}

  /**
   * Constructs an {@code DownloadUrl}.
   *
   * @param url the url
   * @param path the path
   * @param replaceExisting whether to replace existing files
   */
  public DownloadUrl(String url, Path path, boolean replaceExisting) {
    this.url = url;
    this.path = path;
    this.replaceExisting = replaceExisting;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Path getPath() {
    return path;
  }

  public void setPath(Path path) {
    this.path = path;
  }

  public Boolean getReplaceExisting() {
    return replaceExisting;
  }

  public void setReplaceExisting(Boolean replaceExisting) {
    this.replaceExisting = replaceExisting;
  }

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var targetUrl = new URL(url);
    var targetPath = path.toAbsolutePath();

    if (Files.exists(targetPath) && !replaceExisting) {
      logger.info("Skipping download of {} to {}", url, path);
      return;
    }

    if (isHttp(targetUrl)) {
      var get = (HttpURLConnection) targetUrl.openConnection();
      get.setInstanceFollowRedirects(true);
      get.setRequestMethod("GET");
      urlDownloadToFile(get, targetPath);
      get.disconnect();
    } else if (isFtp(targetUrl)) {
      urlDownloadToFile(targetUrl.openConnection(), targetPath);
    } else {
      throw new IllegalArgumentException("Unsupported URL protocol (supported: http(s)/ftp)");
    }
  }

  private static boolean isHttp(URL url) {
    return url.getProtocol().equalsIgnoreCase(PROTOCOL_HTTP) ||
        url.getProtocol().equalsIgnoreCase(PROTOCOL_HTTPS);
  }

  private static boolean isFtp(URL url) {
    return url.getProtocol().equalsIgnoreCase(PROTOCOL_FTP);
  }

  private static void urlDownloadToFile(URLConnection url, Path targetPath) throws IOException {
    try (var inputStream = url.getInputStream()) {
      Files.createDirectories(targetPath.toAbsolutePath().getParent());
      Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
