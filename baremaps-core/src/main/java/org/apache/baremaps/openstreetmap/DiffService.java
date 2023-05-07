/*
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

package org.apache.baremaps.openstreetmap;

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import org.apache.baremaps.collection.DataMap;
import org.apache.baremaps.openstreetmap.function.EntityGeometryBuilder;
import org.apache.baremaps.openstreetmap.function.EntityToGeometryMapper;
import org.apache.baremaps.openstreetmap.model.*;
import org.apache.baremaps.openstreetmap.repository.HeaderRepository;
import org.apache.baremaps.openstreetmap.repository.Repository;
import org.apache.baremaps.openstreetmap.xml.XmlChangeReader;
import org.apache.baremaps.stream.StreamException;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.utils.ProjectionTransformer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffService implements Callable<List<TileCoord>> {

  private static final Logger logger = LoggerFactory.getLogger(DiffService.class);

  private final DataMap<Coordinate> coordinateMap;
  private final DataMap<List<Long>> referenceMap;
  private final HeaderRepository headerRepository;
  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;
  private final int srid;
  private final int zoom;

  public DiffService(DataMap<Coordinate> coordinateMap, DataMap<List<Long>> referenceMap,
      HeaderRepository headerRepository, Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository, Repository<Long, Relation> relationRepository, int srid,
      int zoom) {
    this.coordinateMap = coordinateMap;
    this.referenceMap = referenceMap;
    this.headerRepository = headerRepository;
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
    this.srid = srid;
    this.zoom = zoom;
  }

  @Override
  public List<TileCoord> call() throws Exception {
    logger.info("Importing changes");

    var header = headerRepository.selectLatest();
    var replicationUrl = header.getReplicationUrl();
    var sequenceNumber = header.getReplicationSequenceNumber() + 1;
    var changeUrl = resolve(replicationUrl, sequenceNumber, "osc.gz");

    var projectionTransformer = new ProjectionTransformer(srid, 4326);
    try (var changeInputStream =
        new GZIPInputStream(new BufferedInputStream(changeUrl.openStream()))) {
      return new XmlChangeReader().stream(changeInputStream).flatMap(this::geometriesForChange)
          .map(projectionTransformer::transform).flatMap(this::tilesForGeometry).distinct()
          .toList();
    }
  }

  private Stream<TileCoord> tilesForGeometry(Geometry geometry) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(
            TileCoord.iterator(geometry.getEnvelopeInternal(), zoom, zoom), Spliterator.IMMUTABLE),
        false);
  }

  private Stream<Geometry> geometriesForChange(Change change) {
    switch (change.getType()) {
      case CREATE:
        return geometriesForNextVersion(change);
      case DELETE:
        return geometriesForPreviousVersion(change);
      case MODIFY:
        return Stream.concat(geometriesForPreviousVersion(change),
            geometriesForNextVersion(change));
      default:
        return Stream.empty();
    }
  }

  private Stream<Geometry> geometriesForPreviousVersion(Change change) {
    return change.getEntities().stream().map(this::geometriesForPreviousVersion)
        .flatMap(Optional::stream);
  }

  private Optional<Geometry> geometriesForPreviousVersion(Entity entity) {
    try {
      if (entity instanceof Node node) {
        var previousNode = nodeRepository.get(node.id());
        return Optional.ofNullable(previousNode).map(Node::getGeometry);
      } else if (entity instanceof Way way) {
        var previousWay = wayRepository.get(way.id());
        return Optional.ofNullable(previousWay).map(Way::getGeometry);
      } else if (entity instanceof Relation relation) {
        var previousRelation = relationRepository.get(relation.id());
        return Optional.ofNullable(previousRelation).map(Relation::getGeometry);
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {
      throw new StreamException(e);
    }
  }

  private Stream<Geometry> geometriesForNextVersion(Change change) {
    return change.getEntities().stream()
        .map(consumeThenReturn(new EntityGeometryBuilder(coordinateMap, referenceMap)))
        .flatMap(new EntityToGeometryMapper().andThen(Optional::stream));
  }

  public URL resolve(String replicationUrl, Long sequenceNumber, String extension)
      throws MalformedURLException {
    var s = String.format("%09d", sequenceNumber);
    var uri = String.format("%s/%s/%s/%s.%s", replicationUrl, s.substring(0, 3), s.substring(3, 6),
        s.substring(6, 9), extension);
    return URI.create(uri).toURL();
  }
}
