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

package com.baremaps.osm.reader.xml;

import static com.baremaps.osm.reader.DataFiles.dataOscXml;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.baremaps.osm.model.Change;
import com.baremaps.osm.reader.xml.XmlChangeSpliterator;
import com.baremaps.osm.stream.AccumulatingConsumer;
import com.baremaps.osm.stream.HoldingConsumer;
import java.util.Spliterator;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;

public class XMLChangeSpliteratorTest {

  @Test
  public void tryAdvance() throws XMLStreamException {
    Spliterator<Change> spliterator = new XmlChangeSpliterator(dataOscXml());
    spliterator.forEachRemaining(fileBlock -> assertNotNull(fileBlock));
    assertFalse(spliterator.tryAdvance(new HoldingConsumer<>()));
  }

  @Test
  public void forEachRemaining() throws XMLStreamException {
    Spliterator<Change> spliterator = new XmlChangeSpliterator(dataOscXml());
    AccumulatingConsumer<Change> accumulator = new AccumulatingConsumer<>();
    spliterator.forEachRemaining(accumulator);
    assertEquals(accumulator.values().size(), 51);
  }
  
}
