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

package com.baremaps.cli;

import com.baremaps.blob.BlobStore;
import com.baremaps.blob.BlobStoreRouter;
import com.baremaps.blob.HttpBlobStore;
import com.baremaps.blob.S3BlobStore;
import picocli.CommandLine.Option;

public class Options {

  public enum LogLevel {
    DEBUG,
    INFO,
    TRACE,
    ERROR
  }

  @Option(
      names = {"--log-level"},
      paramLabel = "LOG_LEVEL",
      description = {"The log level."})
  public LogLevel logLevel = LogLevel.INFO;

  @Option(
      names = {"--enable-http"},
      paramLabel = "ENABLE_HTTP",
      description = "Enable Amazon HTTP storage.")
  public boolean enableHTTP = false;

  @Option(
      names = {"--enable-s3"},
      paramLabel = "ENABLE_S3",
      description = "Enable Amazon S3 storage.")
  public boolean enableS3 = false;

  public BlobStore blobStore() {
    BlobStoreRouter blobStore = new BlobStoreRouter();
    if (enableHTTP) {
      blobStore.addScheme("http", new HttpBlobStore());
      blobStore.addScheme("https", new HttpBlobStore());
    }
    if (enableS3) {
      blobStore.addScheme("s3", new S3BlobStore());
    }
    return blobStore;
  }
}
