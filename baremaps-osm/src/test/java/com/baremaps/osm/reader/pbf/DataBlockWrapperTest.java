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

package com.baremaps.osm.reader.pbf;

import static com.baremaps.osm.reader.DataFiles.denseOsmPbf;
import static com.baremaps.osm.reader.DataFiles.relationsOsmPbf;
import static com.baremaps.osm.reader.DataFiles.waysOsmPbf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.stream.HoldingConsumer;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Spliterator;
import org.junit.jupiter.api.Test;

public class DataBlockWrapperTest {

  @Test
  public void readDenseNodes() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    Spliterator<FileBlock> fileBlockIterator = new FileBlockSpliterator(new DataInputStream(denseOsmPbf()));
    fileBlockIterator.tryAdvance(consumer);
    DataBlockReader primitiveBlockReader = new DataBlockReader(consumer.value());
    List<Node> nodes = primitiveBlockReader.getDenseNodes();
    assertNotNull(nodes);
    assertFalse(nodes.isEmpty());
  }

  @Test
  public void readWays() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    Spliterator<FileBlock> fileBlockIterator = new FileBlockSpliterator(new DataInputStream(waysOsmPbf()));
    fileBlockIterator.tryAdvance(consumer);
    DataBlockReader primitiveBlockReader = new DataBlockReader(consumer.value());
    List<Way> ways = primitiveBlockReader.getWays();
    assertNotNull(ways);
    assertFalse(ways.isEmpty());
  }

  @Test
  public void readRelations() throws IOException {
    HoldingConsumer<FileBlock> consumer = new HoldingConsumer<>();
    Spliterator<FileBlock> fileBlockIterator = new FileBlockSpliterator(
        new DataInputStream(relationsOsmPbf()));
    fileBlockIterator.tryAdvance(consumer);
    DataBlockReader primitiveBlockReader = new DataBlockReader(consumer.value());
    List<Relation> relations = primitiveBlockReader.getRelations();
    assertNotNull(relations);
    assertFalse(relations.isEmpty());
  }
}
