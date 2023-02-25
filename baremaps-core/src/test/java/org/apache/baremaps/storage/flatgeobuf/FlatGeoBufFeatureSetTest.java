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
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

class FlatGeoBufFeatureSetTest {

  @Test
  void getType() throws IOException {
    try (var channel = FileChannel.open(TestFiles.resolve("countries.fgb"))) {
      var featureSet = new FlatGeoBufFeatureSet(channel);
      var featureType = featureSet.getType();
      assertEquals(featureType.name(), null);
      assertEquals(featureType.columns().size(), 2);
    }
  }

  @Test
  void read() throws IOException {
    try (var channel = FileChannel.open(TestFiles.resolve("countries.fgb"))) {
      var featureSet = new FlatGeoBufFeatureSet(channel);
      assertEquals(179, featureSet.read().size());
      assertEquals(179, featureSet.read().stream().count());
    }
  }

  @Test
  void write() throws IOException {
    var file = Files.createTempFile("countries", ".fgb");
    file.toFile().deleteOnExit();
    try (var channel1 = FileChannel.open(TestFiles.resolve("countries.fgb"));
        var channel2 = FileChannel.open(file, StandardOpenOption.WRITE)) {
      var featureSet1 = new FlatGeoBufFeatureSet(channel1);
      var featureList = featureSet1.read().stream().toList();
      var featureSet2 = new FlatGeoBufFeatureSet(channel2, featureSet1.getType());
      featureSet2.write(featureList);
    }

    try (var channel = FileChannel.open(file, StandardOpenOption.READ)) {
      var featureSet = new FlatGeoBufFeatureSet(channel);
      assertEquals(179, featureSet.read().size());
    }
  }


}
