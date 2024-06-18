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

package org.apache.baremaps.flatgeobuf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.flatgeobuf.generated.Feature;
import org.apache.baremaps.flatgeobuf.generated.Header;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

public class FlatGeoBufTest {

  @Test
  void readHeader() throws IOException {
    var file = TestFiles.resolve("baremaps-testing/data/samples/countries.fgb");
    try (var channel = FileChannel.open(file, StandardOpenOption.READ)) {
      Header header = FlatGeoBufReader.readHeader(channel);
      assertNotNull(header);
      assertEquals(179, header.featuresCount());
    }
  }

  @Test
  void readFeature() throws IOException {
    var file = TestFiles.resolve("baremaps-testing/data/samples/countries.fgb");
    try (var channel = FileChannel.open(file, StandardOpenOption.READ)) {

      // Read the header
      Header header = FlatGeoBufReader.readHeader(channel);
      assertNotNull(header);
      assertEquals(179, header.featuresCount());

      FlatGeoBuf.Header headerRecord = FlatGeoBufMapper.asHeaderRecord(header);
      assertNotNull(headerRecord);
      assertEquals(179, headerRecord.featuresCount());

      // Read the index
      FlatGeoBufReader.skipIndex(channel, header);

      // Read the first feature
      ByteBuffer buffer = BufferUtil.createByteBuffer(1 << 16, ByteOrder.LITTLE_ENDIAN);
      List<FlatGeoBuf.Feature> featureList = new ArrayList<>();
      for (int i = 0; i < header.featuresCount(); i++) {
        Feature feature = FlatGeoBufReader.readFeature(channel, buffer);
        featureList.add(FlatGeoBufMapper.asFeatureRecord(header, feature));
        assertNotNull(feature);
      }

      // Check the first feature
      FlatGeoBuf.Feature firstFeature = featureList.get(0);
      assertNotNull(firstFeature);
      assertEquals(2, firstFeature.properties().size());
      assertEquals("ATA", firstFeature.properties().get(0));
      assertEquals("Antarctica", firstFeature.properties().get(1));
      assertNotNull(firstFeature.geometry());
      assertEquals(658, firstFeature.geometry().getNumPoints());

      // Check the last feature
      FlatGeoBuf.Feature lastFeature = featureList.get(178);
      assertNotNull(lastFeature);
      assertEquals(2, lastFeature.properties().size());
      assertEquals("FLK", lastFeature.properties().get(0));
      assertEquals("Falkland Islands", lastFeature.properties().get(1));
      assertNotNull(lastFeature.geometry());
      assertEquals(10, lastFeature.geometry().getNumPoints());

      assertThrows(IOException.class, () -> FlatGeoBufReader.readFeature(channel, buffer));
    }
  }

}
