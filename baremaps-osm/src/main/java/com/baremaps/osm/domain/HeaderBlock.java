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

package com.baremaps.osm.domain;

import com.baremaps.osm.handler.BlockConsumer;
import com.baremaps.osm.handler.BlockFunction;

/** Represents a header block in an OpenStreetMap dataset. */
public class HeaderBlock extends Block {

  private final Header header;

  private final Bound bound;

  public HeaderBlock(Blob blob, Header header, Bound bound) {
    super(blob);
    this.header = header;
    this.bound = bound;
  }

  public Header getHeader() {
    return header;
  }

  public Bound getBound() {
    return bound;
  }

  @Override
  public void visit(BlockConsumer consumer) throws Exception {
    consumer.match(this);
  }

  @Override
  public <T> T visit(BlockFunction<T> function) throws Exception {
    return function.match(this);
  }
}
