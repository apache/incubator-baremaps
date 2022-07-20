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

package com.baremaps.osm.function;

import com.baremaps.osm.model.Block;
import com.baremaps.osm.model.DataBlock;
import com.baremaps.osm.model.HeaderBlock;
import com.baremaps.stream.StreamException;
import java.util.function.Consumer;

/** Represents an operation on blocks of different types. */
public interface BlockConsumer extends Consumer<Block> {

  /** {@inheritDoc} */
  @Override
  default void accept(Block block) {
    try {
      block.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  /**
   * Matches an operation on a {@code HeaderBlock}.
   *
   * @param headerBlock the header block
   * @throws Exception
   */
  void match(HeaderBlock headerBlock) throws Exception;

  /**
   * Matches an operation on a {@code DataBlock}.
   *
   * @param dataBlock the data block
   * @throws Exception
   */
  void match(DataBlock dataBlock) throws Exception;
}
