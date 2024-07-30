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

package org.apache.baremaps.raster.elevation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.locationtech.jts.geom.Geometry;

public class ContourRenderer {

  public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth,
      int targetHeight) {
    Image resultingImage =
        originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
    BufferedImage outputImage =
        new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
    return outputImage;
  }

  public static void main(String[] args) throws IOException {
    var path = Path.of("")
        .toAbsolutePath()
        .resolveSibling("baremaps/baremaps-raster/src/test/resources/fuji.png")
        .toAbsolutePath().toFile();

    var image1 = ImageIO.read(path);
    double[] grid1 = ElevationUtils.imageToGrid(image1);
    List<Geometry> contours1 = new ArrayList<>();
    for (int i = 0; i < 8000; i += 100) {
      contours1.addAll(new ContourTracer(grid1, image1.getWidth(), image1.getHeight(), true, true)
          .traceContours(i));
    }

    // Downscale the image by 16
    var image2 = resizeImage(image1, 32, 32);
    double[] grid2 = ElevationUtils.imageToGrid(image2);
    List<Geometry> contours2 = new ArrayList<>();
    for (int i = 0; i < 8000; i += 100) {
      for (Geometry contour : new ContourTracer(grid2, image2.getWidth(), image2.getHeight(), true,
          true).traceContours(i)) {
        // Upscale the line string by 16
        contour = (org.locationtech.jts.geom.Polygon) contour.clone();
        for (int j = 0; j < contour.getNumPoints(); j++) {
          contour.getCoordinates()[j].x *= 16;
          contour.getCoordinates()[j].y *= 16;
        }
        contours2.add(contour);

      }
    }

    // Create a frame to display the contours
    JFrame frame = new JFrame("Contour Lines");
    frame.setSize(image1.getWidth() + 20, image1.getHeight() + 20);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new ContourCanvas(image1, contours1, contours2));
    frame.setVisible(true);
  }

  // Custom Canvas to draw the contours
  static class ContourCanvas extends Canvas {

    Image image;

    List<Geometry> contours1;

    List<Geometry> contours2;

    public ContourCanvas(Image image, List<Geometry> contours1, List<Geometry> contours2) {
      this.image = image;
      this.contours1 = contours1;
      this.contours2 = contours2;
    }

    @Override
    public void paint(Graphics g) {

      // Draw the image
      g.drawImage(image, 10, 10, null);

      // g.setColor(Color.RED);
      // for (Geometry contour : contours1) {
      // List<Point> points = Stream.of(contour.getCoordinates())
      // .map(p -> new Point((int) p.getX() + 10, (int) p.getY() + 10))
      // .toList();
      // for (int i = 0; i < points.size() - 1; i++) {
      // Point p1 = points.get(i);
      // Point p2 = points.get(i + 1);
      // g.drawLine(p1.x, p1.y, p2.x, p2.y);
      // }
      // }

      g.setColor(Color.RED);
      for (Geometry contour : contours2) {
        List<Point> points = Stream.of(contour.getCoordinates())
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