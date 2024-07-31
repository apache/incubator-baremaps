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

package org.apache.baremaps.cli.raster;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static org.apache.baremaps.utils.ObjectMapperUtils.objectMapper;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.linecorp.armeria.common.*;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.JacksonResponseConverterFunction;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.file.HttpFile;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;
import org.apache.baremaps.maplibre.vectortile.*;
import org.apache.baremaps.raster.elevation.*;
import org.apache.baremaps.server.TileResource;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "hillshade", description = "Start a tile server that computes hillshades.")
public class Hillshade implements Callable<Integer> {

  @Option(names = {"--host"}, paramLabel = "HOST", description = "The host of the server.")
  private String host = "localhost";

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {

    var serverBuilder = Server.builder();
    serverBuilder.http(port);

    var objectMapper = objectMapper();
    var jsonResponseConverter = new JacksonResponseConverterFunction(objectMapper);

    LoadingCache<TileCoord, BufferedImage> cache = Caffeine.newBuilder()
        .maximumSize(1000)
        .build(this::getImage);

    var rasterHillshadeTileStore = new RasterHillshadeTileStore(cache);
    serverBuilder.annotatedService(new HillShadeTileResource(() -> rasterHillshadeTileStore),
        jsonResponseConverter);

    var contourTileStore = new ContourTileStore(cache);
    serverBuilder.annotatedService(new TileResource(() -> contourTileStore),
        jsonResponseConverter);

    var index = HttpFile.of(ClassLoader.getSystemClassLoader(), "/raster/hillshade.html");
    serverBuilder.service("/", index.asService());

    serverBuilder.decorator(CorsService.builderForAnyOrigin()
        .allowAllRequestHeaders(true)
        .allowRequestMethods(
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.DELETE,
            HttpMethod.OPTIONS,
            HttpMethod.HEAD)
        .allowCredentials()
        .exposeHeaders(HttpHeaderNames.LOCATION)
        .newDecorator());

    serverBuilder.serviceUnder("/docs", new DocService());

    serverBuilder.disableServerHeader();
    serverBuilder.disableDateHeader();

    var server = serverBuilder.build();

    var startFuture = server.start();
    startFuture.join();

    var shutdownFuture = server.closeOnJvmShutdown();
    shutdownFuture.join();

    return 0;
  }

  private String url = "https://s3.amazonaws.com/elevation-tiles-prod/terrarium/{z}/{x}/{y}.png";

  public BufferedImage getImage(TileCoord tileCoord) throws IOException {
    var tileUrl = new URL(this.url
        .replace("{z}", String.valueOf(tileCoord.z()))
        .replace("{x}", String.valueOf(tileCoord.x()))
        .replace("{y}", String.valueOf(tileCoord.y())));
    return ImageIO.read(tileUrl);
  }

  public static BufferedImage getKernel(TileCoord tileCoord,
      Function<TileCoord, BufferedImage> provider) {
    BufferedImage z1 =
        provider.apply(new TileCoord(tileCoord.x() - 1, tileCoord.y() - 1, tileCoord.z()));
    BufferedImage z2 =
        provider.apply(new TileCoord(tileCoord.x(), tileCoord.y() - 1, tileCoord.z()));
    BufferedImage z3 =
        provider.apply(new TileCoord(tileCoord.x() + 1, tileCoord.y() - 1, tileCoord.z()));
    BufferedImage z4 =
        provider.apply(new TileCoord(tileCoord.x() - 1, tileCoord.y(), tileCoord.z()));
    BufferedImage z5 = provider.apply(tileCoord);
    BufferedImage z6 =
        provider.apply(new TileCoord(tileCoord.x() + 1, tileCoord.y(), tileCoord.z()));
    BufferedImage z7 =
        provider.apply(new TileCoord(tileCoord.x() - 1, tileCoord.y() + 1, tileCoord.z()));
    BufferedImage z8 =
        provider.apply(new TileCoord(tileCoord.x(), tileCoord.y() + 1, tileCoord.z()));
    BufferedImage z9 =
        provider.apply(new TileCoord(tileCoord.x() + 1, tileCoord.y() + 1, tileCoord.z()));
    int kernelSize = z5.getWidth() * 3;
    BufferedImage kernel = new BufferedImage(kernelSize, kernelSize, z5.getType());
    for (int y = 0; y < z5.getHeight(); y++) {
      for (int x = 0; x < z5.getWidth(); x++) {
        kernel.setRGB(x, y, z1.getRGB(x, y));
        kernel.setRGB(x + z5.getWidth(), y, z2.getRGB(x, y));
        kernel.setRGB(x + 2 * z5.getWidth(), y, z3.getRGB(x, y));
        kernel.setRGB(x, y + z5.getHeight(), z4.getRGB(x, y));
        kernel.setRGB(x + z5.getWidth(), y + z5.getHeight(), z5.getRGB(x, y));
        kernel.setRGB(x + 2 * z5.getWidth(), y + z5.getHeight(), z6.getRGB(x, y));
        kernel.setRGB(x, y + 2 * z5.getHeight(), z7.getRGB(x, y));
        kernel.setRGB(x + z5.getWidth(), y + 2 * z5.getHeight(), z8.getRGB(x, y));
        kernel.setRGB(x + 2 * z5.getWidth(), y + 2 * z5.getHeight(), z9.getRGB(x, y));
      }
    }
    return kernel;
  }

  public static class RasterHillshadeTileStore implements TileStore {

    private final LoadingCache<TileCoord, BufferedImage> cache;

    public RasterHillshadeTileStore(LoadingCache<TileCoord, BufferedImage> cache) {
      this.cache = cache;
    }

    @Override
    public ByteBuffer read(TileCoord tileCoord) throws TileStoreException {
      try {

        var image = cache.get(tileCoord);
        var kernel = getKernel(tileCoord, cache::get);
        var buffer = kernel.getSubimage(
            image.getWidth() - 1,
            image.getHeight() - 1,
            image.getWidth() + 2,
            image.getHeight() + 2);

        var grid = ElevationUtils.imageToGrid(buffer, ElevationUtils::pixelToElevationTerrarium);
        var hillshadeGrid =
            new HillshadeCalculator(grid, buffer.getWidth(), buffer.getHeight(), 1, false)
                .calculate(45, 315);

        // Create an output image
        BufferedImage hillshadeImage =
            new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < image.getHeight(); y++) {
          for (int x = 0; x < image.getWidth(); x++) {
            int value = (int) hillshadeGrid[(y + 1) * buffer.getHeight() + x + 1];
            hillshadeImage.setRGB(x, y, new Color(value, value, value).getRGB());
          }
        }

        try (var baos = new ByteArrayOutputStream()) {
          ImageIO.write(hillshadeImage, "png", baos);
          baos.flush();
          return ByteBuffer.wrap(baos.toByteArray());
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void write(TileCoord tileCoord, ByteBuffer blob) throws TileStoreException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void delete(TileCoord tileCoord) throws TileStoreException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws Exception {
      // Do nothing
    }
  }

  public class HillShadeTileResource {

    private static final Logger logger =
        LoggerFactory.getLogger(org.apache.baremaps.server.TileResource.class);

    // public static final String TILE_ENCODING = "gzip";

    public static final String TILE_TYPE = "image/png";

    private final Supplier<TileStore> tileStoreSupplier;

    public HillShadeTileResource(Supplier<TileStore> tileStoreSupplier) {
      this.tileStoreSupplier = tileStoreSupplier;
    }

    @Get("regex:^/raster/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).png")
    @Blocking
    public HttpResponse tile(@Param("z") int z, @Param("x") int x, @Param("y") int y) {
      TileCoord tileCoord = new TileCoord(x, y, z);
      try {
        TileStore tileStore = tileStoreSupplier.get();
        ByteBuffer blob = tileStore.read(tileCoord);
        if (blob != null) {
          var headers = ResponseHeaders.builder(200)
              .add(CONTENT_TYPE, TILE_TYPE)
              // .add(CONTENT_ENCODING, TILE_ENCODING)
              .add(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
              .build();
          byte[] bytes = new byte[blob.remaining()];
          blob.get(bytes);
          HttpData data = HttpData.wrap(bytes);
          return HttpResponse.of(headers, data);
        } else {
          return HttpResponse.of(204);
        }
      } catch (TileStoreException ex) {
        logger.error("Error while reading tile.", ex);
        return HttpResponse.of(404);
      }
    }
  }

  public static class ContourTileStore implements TileStore {

    private final LoadingCache<TileCoord, BufferedImage> cache;

    public ContourTileStore(LoadingCache<TileCoord, BufferedImage> cache) {
      this.cache = cache;
    }

    @Override
    public ByteBuffer read(TileCoord tileCoord) throws TileStoreException {
      var image = cache.get(tileCoord);

      var kernel = getKernel(tileCoord, cache::get);

      image = kernel.getSubimage(
          image.getWidth() - 4,
          image.getHeight() - 4,
          image.getWidth() + 8,
          image.getHeight() + 8);

      var grid = ElevationUtils.imageToGrid(image, ElevationUtils::pixelToElevationTerrarium);

      var features = new ArrayList<Feature>();

      for (int level = -10000; level < 10000; level += 100) {
        var contours = new ContourTracer(grid, image.getWidth(), image.getHeight(), false, false)
            .traceContours(level);

        for (var contour : contours) {

          contour = AffineTransformation
              .translationInstance(-4, -4)
              .scale(16, 16)
              .transform(contour);

          // contour = new ChaikinSmoother(2, 0.25).transform(contour);

          features.add(new Feature(level, Map.of("level", String.valueOf(level)), contour));
        }
      }

      var layer = new Layer("elevation", 4096, features);
      var tile = new Tile(List.of(layer));
      var vectorTile = new VectorTileEncoder().encodeTile(tile);
      try (var baos = new ByteArrayOutputStream()) {
        var gzip = new GZIPOutputStream(baos);
        vectorTile.writeTo(gzip);
        gzip.close();
        return ByteBuffer.wrap(baos.toByteArray());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }

    @Override
    public void write(TileCoord tileCoord, ByteBuffer blob) throws TileStoreException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void delete(TileCoord tileCoord) throws TileStoreException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws Exception {
      // Do nothing
    }
  }

}
