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
import java.util.function.Function;

/**
 * Represents an function on blocks of different types.
 *
 * @param <T>
 */
public interface BlockFunction<T> extends Function<Block, T> {

  /** {@inheritDoc} */
  @Override
  default T apply(Block block) {
    try {
      return block.visit(this);
    } catch (StreamException e) {
      throw e;
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  /**
   * Applies a function on a {@code HeaderBlock}.
   *
   * @param headerBlock the header block
   * @throws Exception
   */
  T match(HeaderBlock headerBlock) throws Exception;

  /**
   * Applies a function on a {@code DataBlock}.
   *
   * @param dataBlock
   * @return the function result
   * @throws Exception
   */
  T match(DataBlock dataBlock) throws Exception;
}
