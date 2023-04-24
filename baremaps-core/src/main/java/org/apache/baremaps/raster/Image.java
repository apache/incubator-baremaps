package org.apache.baremaps.raster;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;


public class Image {

    public static void main(String[] args) throws IOException {
        String urlString = String.format("https://s3.amazonaws.com/elevation-tiles-prod/geotiff/%s/%s/%s.tif", 14, 8492, 5792);
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
