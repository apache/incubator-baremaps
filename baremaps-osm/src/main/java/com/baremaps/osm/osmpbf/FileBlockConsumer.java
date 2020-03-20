/*
 * Copyright (C) 2011 The Baremaps Authors
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

import java.util.function.Consumer;

public abstract class FileBlockConsumer implements Consumer<FileBlock> {

  @Override
  public void accept(FileBlock block) {
    switch (block.getType()) {
      case OSMHeader:
        accept(block.toHeaderBlock());
        break;
      case OSMData:
        accept(block.toPrimitiveBlock());
        break;
      default:
        break;
    }
  }

  public abstract void accept(HeaderBlock headerBlock);

  public abstract void accept(PrimitiveBlock primitiveBlock);
}
