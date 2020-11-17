package com.baremaps.importer.database;

import com.baremaps.importer.geometry.ProjectionHandler;
import com.baremaps.osm.ChangeHandler;
import com.baremaps.osm.ElementHandler;
import com.baremaps.osm.domain.Change;
import com.baremaps.osm.domain.Element;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import com.baremaps.util.tile.Tile;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Geometry;

public class TileHandler implements ChangeHandler {

  private final ProjectionHandler projectionHandler;

  private final NodeTable nodeTable;

  private final WayTable wayTable;

  private final RelationTable relationTable;

  private final int zoom;

  private final Set<Tile> tiles = new HashSet<>();

  public TileHandler(
      NodeTable nodeTable,
      WayTable wayTable,
      RelationTable relationTable,
      ProjectionHandler projectionHandler,
      int zoom) {
    this.projectionHandler = projectionHandler;
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
      tiles.addAll(
          Tile.getTiles(projectionHandler.transform(geometry).getEnvelopeInternal(), zoom)
              .collect(Collectors.toList()));
    }
  }

  public Set<Tile> getTiles() {
    return tiles;
  }

}
