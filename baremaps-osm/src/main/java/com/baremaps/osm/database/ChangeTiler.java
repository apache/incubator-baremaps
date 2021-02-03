package com.baremaps.osm.database;

import com.baremaps.osm.geometry.ProjectionTransformer;
import com.baremaps.osm.ChangeHandler;
import com.baremaps.osm.ElementHandler;
import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Element;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.core.tile.Tile;
import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

public class ChangeTiler implements ChangeHandler {

  private final ProjectionTransformer projectionTransformer;

  private final NodeTable nodeTable;

  private final WayTable wayTable;

  private final RelationTable relationTable;

  private final int zoom;

  private final Set<Tile> tiles = new HashSet<>();

  public ChangeTiler(
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
    for (Element element : change.getElements()) {
      element.accept(new ElementHandler() {
        @Override
        public void handle(Node node) throws Exception {
          try {
            handleGeometry(nodeTable.select(element.getId()).getGeometry());
          } catch (IllegalArgumentException e) {}
        }

        @Override
        public void handle(Way way) throws Exception {
          try {
            handleGeometry(wayTable.select(element.getId()).getGeometry());
          } catch (IllegalArgumentException e) {}
        }

        @Override
        public void handle(Relation relation) throws Exception {
          try {
            handleGeometry(relationTable.select(element.getId()).getGeometry());
          } catch (IllegalArgumentException e) {}
        }
      });
    }
  }

  private void handleNextVersion(Change change) {
    for (Element element : change.getElements()) {
      handleGeometry(element.getGeometry());
    }
  }

  private void handleGeometry(Geometry geometry) {
    if (geometry != null) {
      Envelope overlappingEnvelope = projectionTransformer.transform(geometry).getEnvelopeInternal();
      List<Tile> overlappingTiles = ImmutableList.copyOf(Tile.iterator(overlappingEnvelope, zoom, zoom));
      tiles.addAll(overlappingTiles);
    }
  }

  public Set<Tile> getTiles() {
    return tiles;
  }

  // TODO:
  // Create an abstract method to handle tiles

}
