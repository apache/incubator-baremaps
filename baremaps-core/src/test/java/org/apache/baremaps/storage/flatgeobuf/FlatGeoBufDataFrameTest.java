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

package org.apache.baremaps.storage.flatgeobuf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class FlatGeoBufDataFrameTest {

  @Test
  void rowType() throws IOException {
    var table =
        new FlatGeoBufDataFrame(TestFiles.resolve("baremaps-testing/data/samples/countries.fgb"));
    var rowType = table.schema();
    assertEquals(rowType.name(), null);
    assertEquals(rowType.columns().size(), 2);
  }

  @Test
  void read() throws IOException {
    var table =
        new FlatGeoBufDataFrame(TestFiles.resolve("baremaps-testing/data/samples/countries.fgb"));
    assertEquals(179, table.size());
    assertEquals(179, table.stream().count());
  }

  @Test
  void write() throws IOException {
    var file = Files.createTempFile("countries", ".fgb");
    file.toFile().deleteOnExit();
    var table1 =
        new FlatGeoBufDataFrame(TestFiles.resolve("baremaps-testing/data/samples/countries.fgb"));
    var table2 = new FlatGeoBufDataFrame(file, table1.schema());
    table2.write(table1);

    var featureSet = new FlatGeoBufDataFrame(file);
    assertEquals(179, featureSet.stream().count());
  }
}
