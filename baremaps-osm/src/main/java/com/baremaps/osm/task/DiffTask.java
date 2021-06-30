package com.baremaps.osm.task;

import com.baremaps.blob.BlobStore;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.database.HeaderTable;
import com.baremaps.osm.database.NodeTable;
import com.baremaps.osm.database.RelationTable;
import com.baremaps.osm.database.WayTable;
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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffTask implements Task {

  private static Logger logger = LoggerFactory.getLogger(UpdateTask.class);

  private final BlobStore blobStore;
  private final Cache<Long, Coordinate> coordinateCache;
  private final Cache<Long, List<Long>> referenceCache;
  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;
  private final int srid;
  private final int zoom;
  private final PrintWriter printWriter;
  private final ProjectionTransformer projectionTransformer;

  public DiffTask(
      BlobStore blobStore,
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache,
      HeaderTable headerTable,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable,
      PrintWriter printWriter, int srid,
      int zoom) {
    this.blobStore = blobStore;
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
    this.srid = srid;
    this.zoom = zoom;
    this.printWriter = printWriter;
    this.projectionTransformer = new ProjectionTransformer(srid, 4326);
  }

  @Override
  public Void call() throws Exception {
    logger.info("Importing changes");

    Header header = headerTable.latest();
    String replicationUrl = header.getReplicationUrl();
    Long sequenceNumber = header.getReplicationSequenceNumber() + 1;
    URI changeUri = resolve(replicationUrl, sequenceNumber, "osc.gz");

    GeometryHandler geometryHandler = new GeometryHandler(coordinateCache, referenceCache);
    ProgressLogger progressLogger = new ProgressLogger(blobStore.size(changeUri), 5000);

    try (InputStream changesInputStream = new GZIPInputStream(
        new InputStreamProgress(blobStore.read(changeUri), progressLogger))) {
      OpenStreetMap.streamXmlChanges(changesInputStream)
          .peek(change -> change.getElements().forEach(geometryHandler))
          .flatMap(this::tilesForChange)
          .forEach(tile -> printWriter.println(String.format("%d/%d/%d", tile.x(), tile.y(), tile.z())));
    }

    return null;
  }

  private Stream<Tile> tilesForChange(Change change) {
    switch (change.getType()) {
      case create:
        return tilesForNextVersion(change);
      case delete:
        return tilesForPreviousVersion(change);
      case modify:
        return Stream.concat(tilesForPreviousVersion(change), tilesForNextVersion(change));
      default:
        return Stream.empty();
    }
  }

  private Stream<Tile> tilesForPreviousVersion(Change change) {
    return change.getElements().stream()
        .map(new EntityMapper<Optional<Geometry>>() {
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
            return Optional.ofNullable(nodeTable.select(node.getId())).map(n -> n.getGeometry());
          }

          @Override
          public Optional<Geometry> map(Way way) throws Exception {
            return Optional.ofNullable(wayTable.select(way.getId())).map(w -> w.getGeometry());
          }

          @Override
          public Optional<Geometry> map(Relation relation) throws Exception {
            return Optional.ofNullable(relationTable.select(relation.getId())).map(r -> r.getGeometry());
          }
        })
        .flatMap(optional -> optional.stream())
        .map(geometry -> projectionTransformer.transform(geometry))
        .flatMap(geometry -> Streams.stream(Tile.iterator(geometry.getEnvelopeInternal(), zoom, zoom)));
  }

  private Stream<Tile> tilesForNextVersion(Change change) {
    return change.getElements().stream()
        .flatMap(element -> Optional.ofNullable(element).map(e -> e.getGeometry()).stream())
        .flatMap(geometry -> Streams.stream(Tile.iterator(geometry.getEnvelopeInternal(), zoom, zoom)));
  }

  public URI resolve(String replicationUrl, Long sequenceNumber, String extension) throws URISyntaxException {
    String s = String.format("%09d", sequenceNumber);
    return new URI(String.format("%s/%s/%s/%s.%s",
        replicationUrl,
        s.substring(0, 3),
        s.substring(3, 6),
        s.substring(6, 9),
        extension));
  }

}
