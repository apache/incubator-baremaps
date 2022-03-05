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

package com.baremaps.osm.xml;

import static com.baremaps.testing.TestFiles.DATA_OSM_XML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.baremaps.osm.domain.Entity;
import com.baremaps.stream.AccumulatingConsumer;
import com.baremaps.stream.HoldingConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import org.junit.jupiter.api.Test;

class XmlEntitySpliteratorTest {

  @Test
  void tryAdvance() throws IOException {
    try (InputStream input = DATA_OSM_XML.openStream()) {
      Spliterator<Entity> spliterator = new OsmXmlSpliterator(input);
      spliterator.forEachRemaining(fileBlock -> assertNotNull(fileBlock));
      assertFalse(spliterator.tryAdvance(new HoldingConsumer<>()));
    }
  }

  @Test
  void forEachRemaining() throws IOException {
    try (InputStream input = DATA_OSM_XML.openStream()) {
      Spliterator<Entity> spliterator = new OsmXmlSpliterator(input);
      AccumulatingConsumer<Object> accumulator = new AccumulatingConsumer<>();
      spliterator.forEachRemaining(accumulator);
      assertEquals(12, accumulator.values().size());
    }
  }
}
