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

package org.apache.baremaps.storage.flatgeobuf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class FlatGeoBufDataFrameTest {

  @Test
  void getType() throws IOException {
    var featureSet = new FlatGeoBufDataFrame(TestFiles.resolve("countries.fgb"));
    var featureType = featureSet.getSchema();
    assertEquals(featureType.name(), null);
    assertEquals(featureType.columns().size(), 2);
  }

  @Test
  void read() throws IOException {
    var featureSet = new FlatGeoBufDataFrame(TestFiles.resolve("countries.fgb"));
    assertEquals(179, featureSet.sizeAsLong());
    assertEquals(179, featureSet.stream().count());

  }

  @Test
  void write() throws IOException {
    var file = Files.createTempFile("countries", ".fgb");
    file.toFile().deleteOnExit();
    var featureSet1 = new FlatGeoBufDataFrame(TestFiles.resolve("countries.fgb"));
    var featureList = featureSet1.stream().toList();
    var featureSet2 = new FlatGeoBufDataFrame(file, featureSet1.getSchema());
    featureSet2.write(featureList);

    var featureSet = new FlatGeoBufDataFrame(file);
    assertEquals(179, featureSet.stream().count());
  }


}
