/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.raster;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;


public class Image {

  public static void main(String[] args) throws IOException {
    String urlString = String.format(
        "https://s3.amazonaws.com/elevation-tiles-prod/geotiff/%s/%s/%s.tif", 14, 8492, 5792);
    URL url = new URL(urlString);
    BufferedImage tiffImage = ImageIO.read(url);
    var raster = tiffImage.getRaster();
    for (int x = 0; x < raster.getWidth(); x++) {
      for (int y = 0; y < raster.getHeight(); y++) {
        System.out.println(raster.getSampleFloat(y, y, 0));
      }
    }
  }
}
