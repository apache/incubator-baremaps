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
package com.baremaps.tile;

import java.util.function.Predicate;

public class TileBatcher implements Predicate<Tile> {

  private final int batchArraySize;

  private final int batchArrayIndex;

  public TileBatcher(int batchArraySize, int batchArrayIndex) {
    this.batchArraySize = batchArraySize;
    this.batchArrayIndex = batchArrayIndex;
  }

  @Override
  public boolean test(Tile tile) {
    return batchArraySize <= 1 || tile.index() % batchArraySize == batchArrayIndex;
  }

}
