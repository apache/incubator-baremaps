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

package org.apache.baremaps.server;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferedImageResource {

  private static final Logger logger =
      LoggerFactory.getLogger(BufferedImageResource.class);

  public static final String TILE_TYPE = "image/png";

  private final Supplier<TileStore<BufferedImage>> tileStoreSupplier;

  public BufferedImageResource(Supplier<TileStore<BufferedImage>> tileStoreSupplier) {
    this.tileStoreSupplier = tileStoreSupplier;
  }

  @Get("regex:^/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).png")
  @Blocking
  public HttpResponse tile(@Param("z") int z, @Param("x") int x, @Param("y") int y) {
    TileCoord tileCoord = new TileCoord(x, y, z);
    try {
      TileStore<BufferedImage> tileStore = tileStoreSupplier.get();
      BufferedImage bufferedImage = tileStore.read(tileCoord);
      if (bufferedImage != null) {
        var headers = ResponseHeaders.builder(200)
            .add(CONTENT_TYPE, TILE_TYPE)
            .add(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            .build();

        try (var outputStream = new ByteArrayOutputStream()) {
          ImageIO.write(bufferedImage, "png", outputStream);
          HttpData data = HttpData.wrap(outputStream.toByteArray());
          return HttpResponse.of(headers, data);
        } catch (Exception e) {
          return HttpResponse.of(204);
        }
      } else {
        return HttpResponse.of(204);
      }
    } catch (TileStoreException ex) {
      logger.error("Error while reading tile.", ex);
      return HttpResponse.of(404);
    }
  }
}
