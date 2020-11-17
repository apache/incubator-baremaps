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

import com.baremaps.util.storage.BlobStore;
import com.baremaps.util.storage.CompositeBlobStore;
import com.baremaps.util.storage.HttpBlobStore;
import com.baremaps.util.storage.LocalBlobStore;
import com.baremaps.util.storage.S3BlobStore;
import java.util.ArrayList;
import java.util.List;
import picocli.CommandLine.Option;

public class Options {

  public enum Level {
    DEBUG, INFO, TRACE, ERROR
  }

  @Option(
      names = {"--log-level"},
      paramLabel = "LOG_LEVEL",
      description = {"The log level."})
  public Level logLevel = Level.INFO;

  @Option(
      names = {"--enable-aws"},
      paramLabel = "ENABLE_AWS",
      description = "Enable Amazon Web Service integration.")
  public boolean enableAws = false;

  public BlobStore blobStore() {
    List<BlobStore> components = new ArrayList<>();
    components.add(new LocalBlobStore());
    components.add(new HttpBlobStore());
    if (enableAws) {
      components.add(new S3BlobStore());
    }
    return new CompositeBlobStore(components);
  }


}
