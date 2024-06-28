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

package org.apache.baremaps.raster.hillshade;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.apache.baremaps.raster.TerrainUtils;

public class HillShadeRenderer {

  public static void main(String[] args) throws IOException {
    var image = ImageIO.read(
        Path.of("")
            .toAbsolutePath()
            .resolveSibling("baremaps/baremaps-raster/src/test/resources/fuji.png")
            .toAbsolutePath().toFile());
    var grid = TerrainUtils.grid(image);
    var hillshade = HillShade.hillshade(grid, image.getWidth(), 45, 315);


    // Create an output image
    BufferedImage hillshadeImage =
        new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        int shade = (int) hillshade[y * image.getWidth() + x];
        int rgb = new Color(shade, shade, shade).getRGB();
        hillshadeImage.setRGB(x, y, rgb);
      }
    }

    // Display the hillshade image in a JFrame
    JFrame frame = new JFrame("Hillshade Display");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(image.getWidth(), image.getHeight());
    frame.add(new JLabel(new ImageIcon(hillshadeImage)));
    frame.pack();
    frame.setVisible(true);
  }
}
