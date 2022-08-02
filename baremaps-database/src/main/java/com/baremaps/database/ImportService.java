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

package com.baremaps.database;

import static com.baremaps.stream.ConsumerUtils.consumeThenReturn;
import static com.baremaps.stream.StreamUtils.batch;

import com.baremaps.collection.LongDataMap;
import com.baremaps.database.repository.HeaderRepository;
import com.baremaps.database.repository.Repository;
import com.baremaps.osm.function.BlockEntityConsumer;
import com.baremaps.osm.function.CreateGeometryConsumer;
import com.baremaps.osm.function.ReprojectEntityConsumer;
import com.baremaps.osm.model.Block;
import com.baremaps.osm.model.Entity;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.pbf.PbfBlockReader;
import com.baremaps.osm.store.DataStoreConsumer;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import org.locationtech.jts.geom.Coordinate;

public class ImportService implements Callable<Void> {

  private final Path path;
  private final LongDataMap<Coordinate> coordinates;
  private final LongDataMap<List<Long>> references;
  private final HeaderRepository headerRepository;
  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;
  private final int databaseSrid;

  public ImportService(
      Path path,
      LongDataMap<Coordinate> coordinates,
      LongDataMap<List<Long>> references,
      HeaderRepository headerRepository,
      Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository,
      Integer databaseSrid) {
    this.path = path;
    this.coordinates = coordinates;
    this.references = references;
    this.headerRepository = headerRepository;
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
    this.databaseSrid = databaseSrid;
  }

  @Override
  public Void call() throws Exception {
    Consumer<Block> cacheBlock = new DataStoreConsumer(coordinates, references);
    Consumer<Entity> createGeometry = new CreateGeometryConsumer(coordinates, references);
    Consumer<Entity> reprojectGeometry = new ReprojectEntityConsumer(4326, databaseSrid);
    Consumer<Block> prepareGeometries =
        new BlockEntityConsumer(createGeometry.andThen(reprojectGeometry));
    Function<Block, Block> prepareBlock = consumeThenReturn(cacheBlock.andThen(prepareGeometries));
    Consumer<Block> saveBlock =
        new SaveBlockConsumer(headerRepository, nodeRepository, wayRepository, relationRepository);
    try (InputStream inputStream = Files.newInputStream(path)) {
      batch(new PbfBlockReader().stream(inputStream).map(prepareBlock)).forEach(saveBlock);
    }
    return null;
  }
}
