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

package org.apache.baremaps.openstreetmap.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Spliterator;
import org.apache.baremaps.openstreetmap.model.Entity;
import org.apache.baremaps.openstreetmap.stream.AccumulatingConsumer;
import org.apache.baremaps.openstreetmap.stream.HoldingConsumer;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class XmlEntitySpliteratorTest {

  @Test
  void tryAdvance() throws IOException {
    try (InputStream input = Files.newInputStream(TestFiles.SAMPLE_OSM_XML)) {
      Spliterator<Entity> spliterator = new XmlEntitySpliterator(input);
      spliterator.forEachRemaining(Assertions::assertNotNull);
      assertFalse(spliterator.tryAdvance(new HoldingConsumer<>()));
    }
  }

  @Test
  void forEachRemaining() throws IOException {
    try (InputStream input = Files.newInputStream(TestFiles.SAMPLE_OSM_XML)) {
      Spliterator<Entity> spliterator = new XmlEntitySpliterator(input);
      AccumulatingConsumer<Object> accumulator = new AccumulatingConsumer<>();
      spliterator.forEachRemaining(accumulator);
      assertEquals(38, accumulator.values().size());
    }
  }
}
