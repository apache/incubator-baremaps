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

package com.baremaps.osm.model;

import com.baremaps.osm.function.BlockConsumer;
import com.baremaps.osm.function.BlockFunction;

/** Represents a block of data in an OpenStreetMap dataset. */
public abstract class Block {

  private final Blob blob;

  /**
   * Constructs an OpenStreetMap {@code Block} with the specified {@code Blob}.
   *
   * @param blob the blob
   */
  protected Block(Blob blob) {
    this.blob = blob;
  }

  /**
   * Returns the blob.
   *
   * @return the blob
   */
  public Blob getBlob() {
    return blob;
  }

  /**
   * Accepts the specified block consumer.
   *
   * @param consumer the consumer
   * @throws Exception
   */
  public abstract void visit(BlockConsumer consumer) throws Exception;

  /**
   * Applies the specified block function.
   *
   * @param function the function
   * @param <T> the return type of the function
   * @return the function result
   * @throws Exception
   */
  public abstract <T> T visit(BlockFunction<T> function) throws Exception;
}
