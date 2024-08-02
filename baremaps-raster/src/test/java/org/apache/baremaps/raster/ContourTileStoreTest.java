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

import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.locationtech.jts.geom.util.AffineTransformation;

class ContourTileStoreTest {

  public static void main(String[] args) throws IOException {
    var image = ImageIO
        .read(new URL("https://s3.amazonaws.com/elevation-tiles-prod/terrarium/8/131/89.png"));
    var grid = ElevationUtils.imageToGrid(image, ElevationUtils::pixelToElevationTerrarium);
    var tracer = new ContourTracer(grid, image.getWidth(), image.getHeight(), false, true);

    var contours = tracer.traceContours(200).stream()
        .map(AffineTransformation
            .scaleInstance(4, 4)
            .translate(10, 10)::transform)
        .toList();

    JFrame frame = new JFrame("Geometry Drawer");
    GeometryDrawer drawer = new GeometryDrawer(contours);
    frame.add(drawer);
    frame.setSize(1200, 1200);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null); // Center the frame on the screen
    frame.setVisible(true);
  }
}
