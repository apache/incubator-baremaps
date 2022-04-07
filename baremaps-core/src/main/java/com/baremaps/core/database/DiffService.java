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

package com.baremaps.core.database;

import static com.baremaps.stream.ConsumerUtils.consumeThenReturn;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.collection.LongDataMap;
import com.baremaps.core.database.repository.HeaderRepository;
import com.baremaps.core.database.repository.Repository;
import com.baremaps.core.tile.Tile;
import com.baremaps.osm.change.OsmChangeParser;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.function.CreateGeometryConsumer;
import com.baremaps.osm.function.EntityFunction;
import com.baremaps.osm.function.ExtractGeometryFunction;
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.progress.InputStreamProgress;
import com.baremaps.osm.progress.ProgressLogger;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffService implements Callable<List<Tile>> {

  private static final Logger logger = LoggerFactory.getLogger(DiffService.class);

  private final BlobStore blobStore;
  private final CreateGeometryConsumer createGeometryConsumer;
  private final HeaderRepository headerRepository;
  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;
  private final int srid;
  private final int zoom;

  public DiffService(
      BlobStore blobStore,
      LongDataMap<Coordinate> coordinates,
      LongDataMap<List<Long>> references,
      HeaderRepository headerRepository,
      Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository,
      Repository<Long, Relation> relationRepository,
      int srid,
      int zoom) {
    this.blobStore = blobStore;
    this.headerRepository = headerRepository;
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
    this.srid = srid;
    this.zoom = zoom;
    this.createGeometryConsumer = new CreateGeometryConsumer(coordinates, references);
  }

  @Override
  public List<Tile> call() throws Exception {
    logger.info("Importing changes");

    Header header = headerRepository.selectLatest();
    String replicationUrl = header.getReplicationUrl();
    Long sequenceNumber = header.getReplicationSequenceNumber() + 1;
    URI changeUri = resolve(replicationUrl, sequenceNumber, "osc.gz");

    Blob blob = blobStore.get(changeUri);
    ProgressLogger progressLogger = new ProgressLogger(blob.getContentLength(), 5000);
    ProjectionTransformer projectionTransformer = new ProjectionTransformer(srid, 4326);
    try (InputStream changesInputStream =
        new GZIPInputStream(new InputStreamProgress(blob.getInputStream(), progressLogger))) {
      return new OsmChangeParser()
          .changes(changesInputStream)
          .flatMap(this::geometriesForChange)
          .map(projectionTransformer::transform)
          .flatMap(this::tilesForGeometry)
          .distinct()
          .collect(Collectors.toList());
    }
  }

  private Stream<Tile> tilesForGeometry(Geometry geometry) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(
            Tile.iterator(geometry.getEnvelopeInternal(), zoom, zoom), Spliterator.IMMUTABLE),
        false);
  }

  private Stream<Geometry> geometriesForChange(Change change) {
    switch (change.getType()) {
      case CREATE:
        return geometriesForNextVersion(change);
      case DELETE:
        return geometriesForPreviousVersion(change);
      case MODIFY:
        return Stream.concat(
            geometriesForPreviousVersion(change), geometriesForNextVersion(change));
      default:
        return Stream.empty();
    }
  }

  private Stream<Geometry> geometriesForPreviousVersion(Change change) {
    return change.getEntities().stream()
        .map(
            new EntityFunction<Optional<Geometry>>() {
              @Override
              public Optional<Geometry> match(Header header) {
                return Optional.empty();
              }

              @Override
              public Optional<Geometry> match(Bound bound) {
                return Optional.empty();
              }

              @Override
              public Optional<Geometry> match(Node node) throws Exception {
                Node previousNode = nodeRepository.get(node.getId());
                return Optional.ofNullable(previousNode).map(Node::getGeometry);
              }

              @Override
              public Optional<Geometry> match(Way way) throws Exception {
                Way previousWay = wayRepository.get(way.getId());
                return Optional.ofNullable(previousWay).map(Way::getGeometry);
              }

              @Override
              public Optional<Geometry> match(Relation relation) throws Exception {
                Relation previousRelation = relationRepository.get(relation.getId());
                return Optional.ofNullable(previousRelation).map(Relation::getGeometry);
              }
            })
        .flatMap(Optional::stream);
  }

  private Stream<Geometry> geometriesForNextVersion(Change change) {
    return change.getEntities().stream()
        .map(consumeThenReturn(createGeometryConsumer))
        .flatMap(new ExtractGeometryFunction().andThen(Optional::stream));
  }

  private URI resolve(String replicationUrl, Long sequenceNumber, String extension)
      throws URISyntaxException {
    String s = String.format("%09d", sequenceNumber);
    return new URI(
        String.format(
            "%s/%s/%s/%s.%s",
            replicationUrl, s.substring(0, 3), s.substring(3, 6), s.substring(6, 9), extension));
  }
}
