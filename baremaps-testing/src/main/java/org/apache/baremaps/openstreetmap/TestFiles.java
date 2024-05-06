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

package org.apache.baremaps.openstreetmap;



import java.nio.file.Path;

public class TestFiles {

  public static final Path CONFIG_STYLE_JS = resolve("config/style.js");

  public static final Path ARCHIVE_FILE_BZ2 = resolve("archives/file.bz2");

  public static final Path ARCHIVE_FILE_GZ = resolve("archives/file.gz");

  public static final Path ARCHIVE_FILE_TAR_BZ2 = resolve("archives/file.tar.bz2");

  public static final Path ARCHIVE_FILE_TAR_GZ = resolve("archives/file.tar.gz");

  public static final Path ARCHIVE_FILE_ZIP = resolve("archives/file.zip");

  public static Path resolve(String resource) {
    Path cwd = Path.of("").toAbsolutePath();
    Path pathFromRoot = Path.of("baremaps-testing", "data", resource);
    return cwd.resolveSibling(pathFromRoot).toAbsolutePath();
  }
}
