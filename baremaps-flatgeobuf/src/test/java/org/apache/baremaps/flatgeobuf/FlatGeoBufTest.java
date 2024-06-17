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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
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

      // Read the index
      FlatGeoBufReader.skipIndex(channel, header);

      // Read the first feature
      ByteBuffer buffer = BufferUtil.createByteBuffer(1 << 16, ByteOrder.LITTLE_ENDIAN);
      for (int i = 0; i < header.featuresCount(); i++) {
        Feature feature = FlatGeoBufReader.readFeature(channel, buffer);

        System.out.println(FlatGeoBuf.asFeatureRecord(header, feature));

        assertNotNull(feature);
      }
    }
  }


  public static void main(String... args) {
    System.out.println(Long.MAX_VALUE);
    System.out.println(Long.MAX_VALUE >> 32);
  }

}
