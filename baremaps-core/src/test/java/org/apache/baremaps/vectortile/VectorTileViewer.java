/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.vectortile;

import java.awt.*;
import java.awt.Dimension;
import java.net.URL;
import java.nio.ByteBuffer;
import javax.swing.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * A vector tile viewer.
 */
public class VectorTileViewer {

  public static void main(String... args) throws Exception {
    // String arg =
    // args.length > 0 ? args[0] : "baremaps-core/src/test/resources/vectortile/14-8493-5795.mvt";
    // var path = Path.of(arg);
    // try (var input = new GZIPInputStream(Files.newInputStream(path))) {

    var url = new URL("http://localhost:9000/tiles/14/8628/5750.mvt");

    try (var input = url.openStream()) {
      var buffer = ByteBuffer.wrap(input.readAllBytes());
      var parsed = org.apache.baremaps.mvt.binary.VectorTile.Tile.parseFrom(buffer);
      var tile = new VectorTileDecoder().decodeTile(parsed);
      JFrame f = new JFrame("Vector Tile Viewer");
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      f.add(new TilePanel(tile, 1000));
      f.pack();
      f.setVisible(true);
    }
  }

  static class TilePanel extends JPanel {

    private final Tile tile;

    private final int extent;

    public TilePanel(Tile tile, int extent) {
      this.tile = tile;
      this.extent = extent;
    }

    public Dimension getPreferredSize() {
      return new Dimension(extent, extent);
    }

    public void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);
      graphics.setColor(Color.black);

      Graphics2D graphics2D = (Graphics2D) graphics;
      graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      for (var layer : tile.getLayers()) {
        for (var feature : layer.getFeatures()) {
          var geometry = feature.getGeometry();
          paintGeometry(graphics, geometry, layer.getExtent());
        }
      }
    }

    public void paintGeometry(Graphics graphics, Geometry geometry, int extent) {
      switch (geometry.getGeometryType()) {
        case "Point":
          paintPoint(graphics, (Point) geometry, extent);
          break;
        case "LineString":
          paintLineString(graphics, (LineString) geometry, extent);
          break;
        case "Polygon":
          paintPolygon(graphics, (Polygon) geometry, extent);
          break;
        case "MultiPoint":
        case "MultiLineString":
        case "MultiPolygon":
        case "GeometryCollection":
          paintGeometryCollection(graphics, (GeometryCollection) geometry, extent);
          break;
        default:
          throw new IllegalArgumentException(
              "Unknown geometry type: " + geometry.getGeometryType());
      }
    }

    private void paintGeometryCollection(Graphics graphics, GeometryCollection geometryCollection,
        int extent) {
      for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
        paintGeometry(graphics, geometryCollection.getGeometryN(i), extent);
      }
    }

    private void paintPolygon(Graphics graphics, Polygon polygon, int extent) {
      paintLinearRing(graphics, polygon.getExteriorRing(), extent);
      for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
        paintLinearRing(graphics, polygon.getInteriorRingN(i), extent);
      }
    }

    private void paintLinearRing(Graphics graphics, LinearRing polygon, int extent) {
      Coordinate[] coordinates = polygon.getCoordinates();
      for (int i = 0; i < coordinates.length - 1; i++) {
        var c1 = coordinates[i];
        var c2 = coordinates[i + 1];
        var x1 = coord(c1.x, extent, getWidth());
        var y1 = coord(c1.y, extent, getHeight());
        var x2 = coord(c2.x, extent, getWidth());
        var y2 = coord(c2.y, extent, getHeight());
        graphics.drawLine(x1, y1, x2, y2);
      }
    }

    private void paintLineString(Graphics graphics, LineString lineString, int extent) {
      Coordinate[] coordinates = lineString.getCoordinates();
      for (int i = 0; i < coordinates.length - 1; i++) {
        var c1 = coordinates[i];
        var c2 = coordinates[i + 1];
        var x1 = coord(c1.x, extent, getWidth());
        var y1 = coord(c1.y, extent, getHeight());
        var x2 = coord(c2.x, extent, getWidth());
        var y2 = coord(c2.y, extent, getHeight());
        graphics.drawLine(x1, y1, x2, y2);
      }
    }

    private void paintPoint(Graphics graphics, Point point, int extent) {
      var x = coord(point.getX(), extent, getWidth());
      var y = coord(point.getY(), extent, getHeight());
      graphics.fillOval(x - 2, y - 2, 4, 4);
    }

    private int coord(double value, int extent, int size) {
      return (int) (value * size / extent);
    }
  }
}
