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

package com.baremaps.osm.osmpbf;

import static com.baremaps.osm.DataFiles.dataOsmPbf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import org.junit.jupiter.api.Test;

public class FileBlockConsumerTest {

  @Test
  public void accept() {
    HolderFileBlockConsumer consumer = new HolderFileBlockConsumer();
    Spliterator<FileBlock> spliterator = new FileBlockSpliterator(new DataInputStream(dataOsmPbf()));
    spliterator.forEachRemaining(consumer);
    assertEquals(consumer.headerBlocks.size(), 1);
    assertEquals(consumer.primitiveBlocks.size(), 9);
  }

  class HolderFileBlockConsumer extends FileBlockConsumer {

    public List<HeaderBlock> headerBlocks = new ArrayList<>();

    public List<PrimitiveBlock> primitiveBlocks = new ArrayList<>();

    @Override
    public void accept(HeaderBlock headerBlock) {
      headerBlocks.add(headerBlock);
    }

    @Override
    public void accept(PrimitiveBlock primitiveBlock) {
      primitiveBlocks.add(primitiveBlock);
    }
  }
}
