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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class DecompressFileTest {

  @Test
  void decompressBzip2() throws IOException {
    var source = TestFiles.FILE_BZ2;
    var target = Files.createTempFile("baremaps", ".txt");
    DecompressFile.decompressBzip2(source, target);
    assertTrue(Files.readString(target).contains("test"));
  }

  @Test
  void decompressGzip() throws IOException {
    var source = TestFiles.FILE_GZ;
    var target = Files.createTempFile("baremaps", ".txt");
    DecompressFile.decompressGzip(source, target);
    assertTrue(Files.readString(target).contains("test"));
  }

  @Test
  void decompressTarGz() throws IOException {
    var source = TestFiles.FILE_TAR_GZ;
    var target = Files.createTempDirectory("baremaps");
    DecompressFile.decompressTarGz(source, target);
    assertTrue(Files.readString(target.resolve("file.txt")).contains("test"));
  }

  @Test
  void decompressTarBz2() throws IOException {
    var source = TestFiles.FILE_TAR_BZ2;
    var target = Files.createTempDirectory("baremaps");
    DecompressFile.decompressTarBz2(source, target);
    assertTrue(Files.readString(target.resolve("file.txt")).contains("test"));
  }

  @Test
  void decompressZip() throws IOException {
    var source = TestFiles.FILE_ZIP;
    var target = Files.createTempDirectory("baremaps");
    DecompressFile.decompressZip(source, target);
    assertTrue(Files.readString(target.resolve("file.txt")).contains("test"));
  }
}
