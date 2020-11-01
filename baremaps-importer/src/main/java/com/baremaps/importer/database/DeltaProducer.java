package com.baremaps.importer.database;

import com.baremaps.importer.geometry.ProjectionTransformer;
import com.baremaps.osm.ChangeHandler;
import com.baremaps.osm.ElementHandler;
import com.baremaps.osm.model.Change;
import com.baremaps.osm.model.Element;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.util.tile.Tile;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Geometry;

public class DeltaProducer implements ChangeHandler {

  private final ProjectionTransformer projectionTransformer;

  private final NodeTable nodeTable;

  private final WayTable wayTable;

  private final RelationTable relationTable;

  private final int zoom;

  private final Set<Tile> tiles = new HashSet<>();

  public DeltaProducer(
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable,
      ProjectionTransformer projectionTransformer,
      int zoom) {
    this.projectionTransformer = projectionTransformer;
    this.nodeTable = nodeTable;
    this.wayTable = wayTable;
    this.relationTable = relationTable;
    this.zoom = zoom;
  }

  @Override
  public void handle(Change change) throws Exception {
    switch (change.getType()) {
      case create:
        handleNextVersion(change);
        break;
      case delete:
        handleLastVersion(change);
        break;
      case modify:
        handleLastVersion(change);
        handleNextVersion(change);
        break;
    }
  }

  private void handleLastVersion(Change change) throws Exception {
    for (Element element: change.getElements()) {
      element.visit(new ElementHandler() {
        @Override
        public void handle(Node node) throws Exception {
          handleGeometry(nodeTable.select(element.getId()).getGeometry());
        }

        @Override
        public void handle(Way way) throws Exception {
          handleGeometry(wayTable.select(element.getId()).getGeometry());
        }

        @Override
        public void handle(Relation relation) throws Exception {
          handleGeometry(relationTable.select(element.getId()).getGeometry());
        }
      });
    }
  }

  private void handleNextVersion(Change change) {
    for (Element element: change.getElements()) {
      handleGeometry(element.getGeometry());
    }
  }

  private void handleGeometry(Geometry geometry) {
    tiles.addAll(
        Tile.getTiles(projectionTransformer.transform(geometry).getEnvelopeInternal(), zoom)
            .collect(Collectors.toList()));
  }

  public Set<Tile> getTiles() {
    return tiles;
  }

}
