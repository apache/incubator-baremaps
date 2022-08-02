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

import com.baremaps.osm.OsmReader;
import com.baremaps.osm.function.BlockEntityConsumer;
import com.baremaps.osm.model.Entity;
import com.baremaps.stream.StreamException;
import java.io.InputStream;
import java.util.stream.Stream;

/** A utility class for flattening the blocks streamed by a {@link PbfBlockReader}. */
public class PbfEntityReader implements OsmReader<Entity> {

  private final PbfBlockReader reader;

  /**
   * Constructs an entity reader from a block reader.
   *
   * @param reader a block reader
   */
  public PbfEntityReader(PbfBlockReader reader) {
    this.reader = reader;
  }

  /**
   * Creates an ordered stream of entities.
   *
   * @param inputStream an osm pbf {@link InputStream}
   * @return a stream of blocks
   */
  public Stream<Entity> stream(InputStream inputStream) {
    return reader.stream(inputStream)
        .flatMap(
            block -> {
              try {
                Stream.Builder<Entity> entities = Stream.builder();
                block.visit(new BlockEntityConsumer(entities::add));
                return entities.build();
              } catch (Exception e) {
                throw new StreamException(e);
              }
            });
  }
}
