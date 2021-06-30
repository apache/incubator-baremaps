package com.baremaps.osm.database;

import com.baremaps.blob.BlobStore;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.geometry.GeometryHandler;
import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.handler.EntityMapper;
import com.baremaps.osm.progress.InputStreamProgress;
import com.baremaps.osm.progress.ProgressLogger;
import com.baremaps.tile.Tile;
import com.google.common.collect.Streams;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseDiffer implements Callable<List<Tile>> {

  private static Logger logger = LoggerFactory.getLogger(DatabaseUpdater.class);

  private final BlobStore blobStore;
  private final GeometryHandler geometryHandler;
  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;
  private final int srid;
  private final int zoom;

  public DatabaseDiffer(
      BlobStore blobStore,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache,
      HeaderTable headerTable,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable,
      int srid,
      int zoom) {
    this.blobStore = blobStore;
    this.geometryHandler = new GeometryHandler(coordinateCache, referenceCache);
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
    this.srid = srid;
    this.zoom = zoom;
  }

  @Override
  public List<Tile> call() throws Exception {
    logger.info("Importing changes");

    Header header = headerTable.latest();
    String replicationUrl = header.getReplicationUrl();
    Long sequenceNumber = header.getReplicationSequenceNumber() + 1;
    URI changeUri = resolve(replicationUrl, sequenceNumber, "osc.gz");

    ProgressLogger progressLogger = new ProgressLogger(blobStore.size(changeUri), 5000);
    ProjectionTransformer projectionTransformer = new ProjectionTransformer(srid, 4326);

    try (InputStream changesInputStream = new GZIPInputStream(new InputStreamProgress(blobStore.read(changeUri), progressLogger))) {
      return OpenStreetMap.streamXmlChanges(changesInputStream)
          .flatMap(this::geometriesForChange)
          .peek(projectionTransformer::transform)
          .flatMap(this::tilesForGeometry)
          .distinct()
          .collect(Collectors.toList());
    }
  }

  private Stream<Tile> tilesForGeometry(Geometry geometry) {
    return Streams.stream(Tile.iterator(geometry.getEnvelopeInternal(), zoom, zoom));
  }

  private Stream<Geometry> geometriesForChange(Change change) {
    switch (change.getType()) {
      case create:
        return geometriesForNextVersion(change);
      case delete:
        return geometriesForPreviousVersion(change);
      case modify:
        return Stream.concat(geometriesForPreviousVersion(change), geometriesForNextVersion(change));
      default:
        return Stream.empty();
    }
  }

  private Stream<Geometry> geometriesForPreviousVersion(Change change) {
    return change.getElements().stream().map(new EntityMapper<Optional<Geometry>>() {
      @Override
      public Optional<Geometry> map(Header header) {
        return Optional.empty();
      }

      @Override
      public Optional<Geometry> map(Bound bound) {
        return Optional.empty();
      }

      @Override
      public Optional<Geometry> map(Node node) throws Exception {
        Node previousNode = nodeTable.select(node.getId());
        return Optional.ofNullable(previousNode).map(n -> n.getGeometry());
      }

      @Override
      public Optional<Geometry> map(Way way) throws Exception {
        Way previousWay = wayTable.select(way.getId());
        return Optional.ofNullable(previousWay).map(w -> w.getGeometry());
      }

      @Override
      public Optional<Geometry> map(Relation relation) throws Exception {
        Relation previousRelation = relationTable.select(relation.getId());
        return Optional.ofNullable(previousRelation).map(r -> r.getGeometry());
      }
    }).flatMap(optional -> optional.stream());
  }

  private Stream<Geometry> geometriesForNextVersion(Change change) {
    return change.getElements().stream()
        .peek(geometryHandler)
        .map(element -> Optional.ofNullable(element).map(e -> e.getGeometry()))
        .flatMap(optional -> optional.stream());
  }

  private URI resolve(String replicationUrl, Long sequenceNumber, String extension) throws URISyntaxException {
    String s = String.format("%09d", sequenceNumber);
    return new URI(String.format("%s/%s/%s/%s.%s",
        replicationUrl,
        s.substring(0, 3),
        s.substring(3, 6),
        s.substring(6, 9),
        extension));
  }

}
