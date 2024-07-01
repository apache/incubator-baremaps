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

package org.apache.baremaps.raster.contour;

import static org.apache.baremaps.raster.contour.IsoLines.isoLines;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.apache.baremaps.raster.contour.IsoLines.IsoLine;
import org.apache.baremaps.raster.martini.Martini;

public class IsoLineRenderer {

  public static void main(String[] args) throws IOException {
    var path = Path.of("")
        .toAbsolutePath()
        .resolveSibling("baremaps/baremaps-raster/src/test/resources/fuji.png")
        .toAbsolutePath().toFile();

    System.out.println(path);

    var image = ImageIO.read(path);

    double[] grid = Martini.grid(image);
    List<IsoLine> contours = new ArrayList<>();
    for (int i = 0; i < 8000; i += 100) {
      contours.addAll(isoLines(grid, image.getWidth(), i));
    }

    // Create a frame to display the contours
    JFrame frame = new JFrame("Contour Lines");
    frame.setSize(image.getWidth(), image.getHeight());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new ContourCanvas(contours));
    frame.setVisible(true);
  }

  // Custom Canvas to draw the contours
  static class ContourCanvas extends Canvas {
    List<IsoLine> contours;

    public ContourCanvas(List<IsoLine> contours) {
      this.contours = contours;
    }

    @Override
    public void paint(Graphics g) {
      g.setColor(Color.RED);
      for (IsoLine contour : contours) {
        List<Point> points = contour.points()
            .stream().map(p -> new Point((int) p.x(), (int) p.y()))
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
