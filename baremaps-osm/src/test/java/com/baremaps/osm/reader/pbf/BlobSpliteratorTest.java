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

import static com.baremaps.osm.reader.DataFiles.denseNodesOsmPbf;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

public class BlobSpliteratorTest {

  @Test
  public void readDenseNodes() {
    BlobSpliterator spliterator = new BlobSpliterator(denseNodesOsmPbf());
    DataBlock dataBlock = StreamSupport.stream(spliterator, false)
        .map(FileBlockStreamer::toFileBlock)
        .filter(fileBlock -> fileBlock instanceof DataBlock)
        .map(fileBlock -> (DataBlock) fileBlock)
        .findFirst().get();
    List<Node> nodes = dataBlock.getDenseNodes();
    assertNotNull(nodes);
    assertFalse(nodes.isEmpty());
  }

  @Test
  public void readWays() {
    BlobSpliterator spliterator = new BlobSpliterator(waysOsmPbf());
    DataBlock dataBlock = StreamSupport.stream(spliterator, false)
        .map(FileBlockStreamer::toFileBlock)
        .filter(fileBlock -> fileBlock instanceof DataBlock)
        .map(fileBlock -> (DataBlock) fileBlock)
        .findFirst().get();
    List<Way> ways = dataBlock.getWays();
    assertNotNull(ways);
    assertFalse(ways.isEmpty());
  }

  @Test
  public void readRelations() {
    BlobSpliterator spliterator = new BlobSpliterator(relationsOsmPbf());
    DataBlock dataBlock = StreamSupport.stream(spliterator, false)
        .map(FileBlockStreamer::toFileBlock)
        .filter(fileBlock -> fileBlock instanceof DataBlock)
        .map(fileBlock -> (DataBlock) fileBlock)
        .findFirst().get();
    List<Relation> relations = dataBlock.getRelations();
    assertNotNull(relations);
    assertFalse(relations.isEmpty());
  }

}
