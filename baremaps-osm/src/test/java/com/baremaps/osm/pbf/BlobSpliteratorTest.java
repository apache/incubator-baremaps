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

package com.baremaps.osm.pbf;

public class BlobSpliteratorTest {


  /*
  TODO: rewrite these tests
  @Test
  public void readDenseNodes() {
    BlobSpliterator spliterator = new BlobSpliterator(denseNodesOsmPbf());
    StreamSupport.stream(spliterator, false)
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
   */
}
