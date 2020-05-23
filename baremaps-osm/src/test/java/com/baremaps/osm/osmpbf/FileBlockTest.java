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
import static com.baremaps.osm.DataFiles.invalidOsmPbf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baremaps.util.stream.StreamException;
import java.io.DataInputStream;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

public class FileBlockTest {

  @Test
  public void stream() {
    assertTrue(StreamSupport
        .stream(new FileBlockSpliterator(new DataInputStream(dataOsmPbf())), false)
        .count()
        == 10);
  }

  @Test
  public void isHeaderBlock() {
    assertTrue(StreamSupport
        .stream(new FileBlockSpliterator(new DataInputStream(dataOsmPbf())), false)
        .filter(FileBlock::isHeaderBlock)
        .count()
        == 1);
  }

  @Test
  public void isDataBlock() {
    assertTrue(StreamSupport
        .stream(new FileBlockSpliterator(new DataInputStream(dataOsmPbf())), false)
        .filter(FileBlock::isPrimitiveBlock)
        .count()
        == 9);
  }

  @Test
  public void toHeaderBlock() {
    assertTrue(StreamSupport
        .stream(new FileBlockSpliterator(new DataInputStream(dataOsmPbf())), false)
        .filter(FileBlock::isHeaderBlock)
        .map(FileBlock::toHeaderBlock)
        .count()
        == 1);
  }

  @Test
  public void toDataBlock() {
    assertTrue(StreamSupport
        .stream(new FileBlockSpliterator(new DataInputStream(dataOsmPbf())), false)
        .filter(FileBlock::isPrimitiveBlock)
        .map(FileBlock::toPrimitiveBlock)
        .collect(Collectors.toList())
        .size()
        == 9);
  }

  @Test
  public void toDataBlockException() {
    assertThrows(StreamException.class, () -> {
      invalidOsmPbf().toPrimitiveBlock();
    });
  }

}
