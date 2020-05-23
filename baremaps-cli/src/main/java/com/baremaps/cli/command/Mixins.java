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

package com.baremaps.cli.command;

import com.baremaps.cli.option.LevelOption;
import com.baremaps.util.fs.CachedFileSystem;
import com.baremaps.util.fs.CompositeFileSystem;
import com.baremaps.util.fs.FileSystem;
import com.baremaps.util.fs.HttpFileSystem;
import com.baremaps.util.fs.LocalFileSystem;
import com.baremaps.util.fs.S3FileSystem;
import java.util.Arrays;
import java.util.List;
import picocli.CommandLine.Option;

public class Mixins {

  @Option(
      names = {"--log-level"},
      paramLabel = "LOG_LEVEL",
      description = {"The log level."})
  public LevelOption logLevel = LevelOption.INFO;

  @Option(
      names = {"--enable-caching"},
      paramLabel = "ENABLE_CACHING",
      description = "Cache downloaded resources in temporary files.")
  public boolean enableCaching = false;

  @Option(
      names = {"--enable-aws"},
      paramLabel = "ENABLE_AWS",
      description = "Enable Amazon Web Service integration.")
  public boolean enableAws = false;

  public FileSystem filesystem() {
    List<FileSystem> components = Arrays.asList(new LocalFileSystem(), new HttpFileSystem());
    if (enableAws) {
      components.add(new S3FileSystem());
    }
    FileSystem fileSystem = new CompositeFileSystem(components);
    if (enableCaching) {
      fileSystem = new CachedFileSystem(fileSystem);
    }
    return fileSystem;
  }

}
