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

package org.apache.baremaps.database;

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

import java.io.BufferedInputStream;
import java.io.InputStream;
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
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.database.repository.HeaderRepository;
import org.apache.baremaps.database.repository.Repository;
import org.apache.baremaps.database.tile.Tile;
import org.apache.baremaps.osm.function.CreateGeometryConsumer;
import org.apache.baremaps.osm.function.EntityFunction;
import org.apache.baremaps.osm.function.ExtractGeometryFunction;
import org.apache.baremaps.osm.geometry.ProjectionTransformer;
import org.apache.baremaps.osm.model.Bound;
import org.apache.baremaps.osm.model.Change;
import org.apache.baremaps.osm.model.Header;
import org.apache.baremaps.osm.model.Node;
import org.apache.baremaps.osm.model.Relation;
import org.apache.baremaps.osm.model.Way;
import org.apache.baremaps.osm.xml.XmlChangeReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffService implements Callable<List<Tile>> {

  private static final Logger logger = LoggerFactory.getLogger(DiffService.class);

  private final LongDataMap<Coordinate> coordinates;
  private final LongDataMap<List<Long>> references;
  private final HeaderRepository headerRepository;
  private final Repository<Long, Node> nodeRepository;
  private final Repository<Long, Way> wayRepository;
  private final Repository<Long, Relation> relationRepository;
  private final int srid;
  private final int zoom;

  public DiffService(LongDataMap<Coordinate> coordinates, LongDataMap<List<Long>> references,
      HeaderRepository headerRepository, Repository<Long, Node> nodeRepository,
      Repository<Long, Way> wayRepository, Repository<Long, Relation> relationRepository, int srid,
      int zoom) {
    this.coordinates = coordinates;
    this.references = references;
    this.headerRepository = headerRepository;
    this.nodeRepository = nodeRepository;
    this.wayRepository = wayRepository;
    this.relationRepository = relationRepository;
    this.srid = srid;
    this.zoom = zoom;
  }

  @Override
  public List<Tile> call() throws Exception {
    logger.info("Importing changes");

    Header header = headerRepository.selectLatest();
    String replicationUrl = header.getReplicationUrl();
    Long sequenceNumber = header.getReplicationSequenceNumber() + 1;
    URL changeUrl = resolve(replicationUrl, sequenceNumber, "osc.gz");

    ProjectionTransformer projectionTransformer = new ProjectionTransformer(srid, 4326);
    try (InputStream changeInputStream =
        new GZIPInputStream(new BufferedInputStream(changeUrl.openStream()))) {
      return new XmlChangeReader().stream(changeInputStream).flatMap(this::geometriesForChange)
          .map(projectionTransformer::transform).flatMap(this::tilesForGeometry).distinct()
          .toList();
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
        return Stream.concat(geometriesForPreviousVersion(change),
            geometriesForNextVersion(change));
      default:
        return Stream.empty();
    }
  }

  private Stream<Geometry> geometriesForPreviousVersion(Change change) {
    return change.getEntities().stream().map(new EntityFunction<Optional<Geometry>>() {
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
    }).flatMap(Optional::stream);
  }

  private Stream<Geometry> geometriesForNextVersion(Change change) {
    return change.getEntities().stream()
        .map(consumeThenReturn(new CreateGeometryConsumer(coordinates, references)))
        .flatMap(new ExtractGeometryFunction().andThen(Optional::stream));
  }

  public URL resolve(String replicationUrl, Long sequenceNumber, String extension)
      throws MalformedURLException {
    String s = String.format("%09d", sequenceNumber);
    String uri = String.format("%s/%s/%s/%s.%s", replicationUrl, s.substring(0, 3),
        s.substring(3, 6), s.substring(6, 9), extension);
    return URI.create(uri).toURL();
  }
}
