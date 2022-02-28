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

package com.baremaps.osm.repository;

import static com.baremaps.osm.OpenStreetMap.streamPbfBlocks;
import static com.baremaps.stream.ConsumerUtils.consumeThenReturn;
import static com.baremaps.stream.StreamUtils.batch;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.function.BlockEntityConsumer;
import com.baremaps.osm.geometry.CreateGeometryConsumer;
import com.baremaps.osm.geometry.ReprojectEntityConsumer;
import com.baremaps.osm.progress.InputStreamProgress;
import com.baremaps.osm.progress.ProgressLogger;
import com.baremaps.osm.store.DataStoreConsumer;
import com.baremaps.store.LongDataMap;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportService implements Callable<Void> {

  private static final Logger logger = LoggerFactory.getLogger(ImportService.class);

  private final URI uri;
  private final BlobStore blobStore;
  private final LongDataMap<Coordinate> coordinateCache;
  private final LongDataMap<List<Long>> referenceCache;
  private final HeaderRepository headerRepository;
  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;
  private final int srid;
  private final int threads;

  public ImportService(
      URI uri,
      BlobStore blobStore,
      LongDataMap<Coordinate> coordinateCache,
      LongDataMap<List<Long>> referenceCache,
      HeaderRepository headerRepository,
      Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository,
      int srid,
      int threads) {
    this.uri = uri;
    this.blobStore = blobStore;
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
    this.headerRepository = headerRepository;
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
    this.srid = srid;
    this.threads = threads;
  }

  @Override
  public Void call() throws Exception {
    Consumer<Block> cacheBlock = new DataStoreConsumer(coordinateCache, referenceCache);
    Consumer<Entity> createGeometry = new CreateGeometryConsumer(coordinateCache, referenceCache);
    Consumer<Entity> reprojectGeometry = new ReprojectEntityConsumer(4326, srid);
    Consumer<Block> prepareGeometries =
        new BlockEntityConsumer(createGeometry.andThen(reprojectGeometry));
    Function<Block, Block> prepareBlock = consumeThenReturn(cacheBlock.andThen(prepareGeometries));
    Consumer<Block> saveBlock =
        new SaveBlockConsumer(headerRepository, nodeRepository, wayRepository, relationRepository);

    Blob blob = blobStore.get(uri);
    ProgressLogger progressLogger = new ProgressLogger(blob.getContentLength(), 5000);
    try (InputStream inputStream = new InputStreamProgress(blob.getInputStream(), progressLogger)) {
      batch(streamPbfBlocks(inputStream).map(prepareBlock)).forEach(saveBlock);
    }

    return null;
  }
}
