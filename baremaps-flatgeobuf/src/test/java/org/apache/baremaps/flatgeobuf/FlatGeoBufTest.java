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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.flatgeobuf.generated.Feature;
import org.apache.baremaps.flatgeobuf.generated.Header;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Test;

public class FlatGeoBufTest {

  @Test
  void readWriteFlatGeoBuf() throws IOException {
    Path file = TestFiles.resolve("baremaps-testing/data/samples/countries.fgb");
    Path tempFile = Files.createTempFile("countries", ".fgb");

    Header headerFlatGeoBuf1 = null;
    FlatGeoBuf.Header headerRecord1 = null;
    List<FlatGeoBuf.Feature> featureRecords = new ArrayList<>();

    // Copy the file
    try (ReadableByteChannel channel = FileChannel.open(file, StandardOpenOption.READ);
        WritableByteChannel tempChannel = FileChannel.open(tempFile, StandardOpenOption.WRITE)) {

      // Copy the header
      headerFlatGeoBuf1 = FlatGeoBufReader.readHeaderFlatGeoBuf(channel);
      headerRecord1 = FlatGeoBufReader.asRecord(headerFlatGeoBuf1);
      FlatGeoBufWriter.writeHeaderFlatGeoBuf(tempChannel, headerFlatGeoBuf1);

      // Copy the index
      ByteBuffer indexBuffer = FlatGeoBufReader.readIndexBuffer(channel, headerFlatGeoBuf1);
      FlatGeoBufWriter.writeIndexBuffer(tempChannel, indexBuffer);

        // Copy the features
      var buffer = ByteBuffer.allocate(1 << 10).order(ByteOrder.LITTLE_ENDIAN);
      for (int i = 0; i < headerFlatGeoBuf1.featuresCount(); i++) {
        Feature feature = FlatGeoBufReader.readFeatureFlatGeoBuf(channel, buffer);
        FlatGeoBufWriter.writeFeatureFlatGeoBuf(tempChannel, feature);
        FlatGeoBuf.Feature featureRecord = FlatGeoBufWriter.writeFeatureFlatGeoBuf(headerFlatGeoBuf1, feature);
        featureRecords.add(featureRecord);
      }
    }

    // Read the copied file
    try (var channel = FileChannel.open(tempFile, StandardOpenOption.READ)) {

      // Read the header
      Header headerFlatGeoBuf2 = FlatGeoBufReader.readHeaderFlatGeoBuf(channel);
      FlatGeoBuf.Header headerRecord2 = FlatGeoBufReader.asRecord(headerFlatGeoBuf2);
      assertNotNull(headerFlatGeoBuf2);
      assertEquals(headerRecord1, headerRecord2);

      // Read the index
      FlatGeoBufReader.skipIndex(channel, headerFlatGeoBuf2);

      // Read the features
      ByteBuffer buffer = ByteBuffer.allocate(1 << 10).order(ByteOrder.LITTLE_ENDIAN);
      for (int i = 0; i < headerFlatGeoBuf2.featuresCount(); i++) {
        Feature featureFlatGeoBuf = FlatGeoBufReader.readFeatureFlatGeoBuf(channel, buffer);
        FlatGeoBuf.Feature featureRecord = FlatGeoBufWriter.writeFeatureFlatGeoBuf(headerFlatGeoBuf2, featureFlatGeoBuf);
        assertNotNull(featureRecord);
        assertEquals(featureRecords.get(i), featureRecord);
      }
    }
  }

  @Test
  void readWriteRecord() throws IOException {
    Path file = TestFiles.resolve("baremaps-testing/data/samples/countries.fgb");
    Path tempFile = Files.createTempFile("countries", ".fgb");

    Header headerFlatGeoBuf1 = null;
    FlatGeoBuf.Header headerRecord1 = null;
    List<FlatGeoBuf.Feature> featureRecords = new ArrayList<>();

    // Copy the file
    try (ReadableByteChannel channel = FileChannel.open(file, StandardOpenOption.READ);
         WritableByteChannel tempChannel = FileChannel.open(tempFile, StandardOpenOption.WRITE)) {

      // Copy the header
      headerRecord1 = FlatGeoBufReader.readHeaderRecord(channel);
      headerFlatGeoBuf1 = FlatGeoBufWriter.asHeaderRecord(headerRecord1);
      FlatGeoBufWriter.writeHeaderRecord(tempChannel, headerRecord1);

      // Copy the index
      ByteBuffer indexBuffer = FlatGeoBufReader.readIndexBuffer(channel, headerFlatGeoBuf1);
      FlatGeoBufWriter.writeIndexBuffer(tempChannel, indexBuffer);

      // Copy the features
      var buffer = ByteBuffer.allocate(1 << 10).order(ByteOrder.LITTLE_ENDIAN);
      for (int i = 0; i < headerFlatGeoBuf1.featuresCount(); i++) {
        FlatGeoBuf.Feature feature = FlatGeoBufReader.readFeatureRecord(channel, headerFlatGeoBuf1, buffer);
        FlatGeoBufWriter.writeFeatureRecord(tempChannel, headerFlatGeoBuf1, feature);
        featureRecords.add(feature);
      }
    }

    // Read the copied file
    try (var channel = FileChannel.open(tempFile, StandardOpenOption.READ)) {

      // Read the header
      FlatGeoBuf.Header headerRecord2 = FlatGeoBufReader.readHeaderRecord(channel);
      Header headerFlatGeoBuf2 = FlatGeoBufWriter.asHeaderRecord(headerRecord2);
      assertNotNull(headerFlatGeoBuf2);
      assertEquals(headerRecord1, headerRecord2);

      // Read the index
      FlatGeoBufReader.skipIndex(channel, headerFlatGeoBuf2);

      // Read the features
      ByteBuffer buffer = ByteBuffer.allocate(1 << 10).order(ByteOrder.LITTLE_ENDIAN);
      for (int i = 0; i < headerFlatGeoBuf2.featuresCount(); i++) {
        FlatGeoBuf.Feature featureRecord = FlatGeoBufReader.readFeatureRecord(channel, headerFlatGeoBuf2, buffer);
        assertNotNull(featureRecord);
        assertEquals(featureRecords.get(i), featureRecord);
      }
    }
  }

}
