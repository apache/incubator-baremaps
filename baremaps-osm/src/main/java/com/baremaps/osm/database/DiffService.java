package com.baremaps.osm.database;

import static com.baremaps.stream.ConsumerUtils.consumeThenReturn;

import com.baremaps.blob.BlobStore;
import com.baremaps.osm.OpenStreetMap;
import com.baremaps.osm.cache.CoordinateCache;
import com.baremaps.osm.cache.ReferenceCache;
import com.baremaps.osm.domain.Bound;
import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Header;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.osm.geometry.CreateGeometryConsumer;
import com.baremaps.osm.geometry.ExtractGeometryFunction;
import com.baremaps.osm.geometry.ReprojectGeometryConsumer;
import com.baremaps.osm.handler.EntityFunction;
import com.baremaps.osm.progress.InputStreamProgress;
import com.baremaps.osm.progress.ProgressLogger;
import com.baremaps.tile.Tile;
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
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffService implements Callable<List<Tile>> {

  private static final Logger logger = LoggerFactory.getLogger(DiffService.class);

  private final BlobStore blobStore;
  private final CreateGeometryConsumer createGeometryConsumer;
  private final HeaderTable headerTable;
  private final NodeTable nodeTable;
  private final WayTable wayTable;
  private final RelationTable relationTable;
  private final int srid;
  private final int zoom;

  public DiffService(
      BlobStore blobStore,
      CoordinateCache coordinateCache,
      ReferenceCache referenceCache,
      HeaderTable headerTable,
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable,
      int srid,
      int zoom) {
    this.blobStore = blobStore;
    this.headerTable = headerTable;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
    this.srid = srid;
    this.zoom = zoom;
    this.createGeometryConsumer = new CreateGeometryConsumer(coordinateCache, referenceCache);
  }

  @Override
  public List<Tile> call() throws Exception {
    logger.info("Importing changes");

    Header header = headerTable.selectLatest();
    String replicationUrl = header.getReplicationUrl();
    Long sequenceNumber = header.getReplicationSequenceNumber() + 1;
    URI changeUri = resolve(replicationUrl, sequenceNumber, "osc.gz");

    ProgressLogger progressLogger = new ProgressLogger(blobStore.size(changeUri), 5000);
    ReprojectGeometryConsumer reprojectGeometryConsumer = new ReprojectGeometryConsumer(srid, 4326);

    try (InputStream changesInputStream = new GZIPInputStream(new InputStreamProgress(blobStore.read(changeUri), progressLogger))) {
      return OpenStreetMap.streamXmlChanges(changesInputStream)
          .flatMap(this::geometriesForChange)
          .map(consumeThenReturn(reprojectGeometryConsumer::transform))
          .flatMap(this::tilesForGeometry)
          .distinct()
          .collect(Collectors.toList());
    }
  }

  private Stream<Tile> tilesForGeometry(Geometry geometry) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
        Tile.iterator(geometry.getEnvelopeInternal(), zoom, zoom),
        Spliterator.IMMUTABLE), false);
  }

  private Stream<Geometry> geometriesForChange(Change change) {
    switch (change.getType()) {
      case CREATE:
        return geometriesForNextVersion(change);
      case DELETE:
        return geometriesForPreviousVersion(change);
      case MODIFY:
        return Stream.concat(geometriesForPreviousVersion(change), geometriesForNextVersion(change));
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
        Node previousNode = nodeTable.select(node.getId());
        return Optional.ofNullable(previousNode).map(Node::getGeometry);
      }

      @Override
      public Optional<Geometry> match(Way way) throws Exception {
        Way previousWay = wayTable.select(way.getId());
        return Optional.ofNullable(previousWay).map(Way::getGeometry);
      }

      @Override
      public Optional<Geometry> match(Relation relation) throws Exception {
        Relation previousRelation = relationTable.select(relation.getId());
        return Optional.ofNullable(previousRelation).map(Relation::getGeometry);
      }
    }).flatMap(Optional::stream);
  }

  private Stream<Geometry> geometriesForNextVersion(Change change) {
    return change.getEntities().stream()
        .map(consumeThenReturn(createGeometryConsumer))
        .flatMap(new ExtractGeometryFunction().andThen(Optional::stream));
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
