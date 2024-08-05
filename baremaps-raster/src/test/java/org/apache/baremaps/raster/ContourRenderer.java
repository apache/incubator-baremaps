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

package org.apache.baremaps.raster;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;

public class ContourRenderer {

  public static void main(String[] args) throws IOException {
    // Load the image
    var path = Path.of("")
        .toAbsolutePath()
        .resolveSibling("baremaps/baremaps-raster/src/test/resources/fuji.png")
        .toAbsolutePath().toFile();
    var image = ImageIO.read(path);

    // Downscale the image by a factor of 16
    image = RasterUtils.resizeImage(image, 32, 32);

    // Convert the image to a grid
    double[] grid = ElevationUtils.imageToGrid(image, ElevationUtils::pixelToElevationNormal);

    List<Geometry> contour =
        new ContourTracer(grid, image.getWidth(), image.getHeight(), true, false)
            .traceContours(0, 9000, 100);

    // Scale the image back to its original size
    image = RasterUtils.resizeImage(image, image.getWidth() * 16, image.getHeight() * 16);

    // Scale the contour back to its original size
    AffineTransformation transformation = AffineTransformation.scaleInstance(16, 16);
    contour = contour.stream()
        .map(transformation::transform)
        .toList();

    // Smooth the contour with the Chaikin algorithm
    ChaikinSmoother smoother = new ChaikinSmoother(1, 0.25);
    contour = contour.stream()
        .map(smoother::transform)
        .toList();

    // Create a frame to display the contours
    JFrame frame = new JFrame("Contour Lines");
    frame.setSize(image.getWidth() + 20, image.getHeight() + 20);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new ContourCanvas(image, contour));
    frame.setVisible(true);
  }

  // Custom Canvas to draw the contours
  static class ContourCanvas extends Canvas {

    Image image;

    List<Geometry> contour;

    public ContourCanvas(Image image, List<Geometry> contour) {
      this.image = image;
      this.contour = contour;
    }

    @Override
    public void paint(Graphics g) {

      // Draw the image
      g.drawImage(image, 10, 10, null);

      g.setColor(Color.RED);
      for (Geometry geometry : contour) {
        List<Point> points = Stream.of(geometry.getCoordinates())
            .map(p -> new Point((int) p.getX() + 10, (int) p.getY() + 10))
            .toList();
        for (int i = 0; i < points.size() - 1; i++) {
          Point p1 = points.get(i);
          Point p2 = points.get(i + 1);
          g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
      }
    }
  }
}
