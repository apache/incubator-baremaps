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

import static com.google.common.net.HttpHeaders.*;

import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource that provides access to the tiles.
 */
public class TileResource {

  private static final Logger logger = LoggerFactory.getLogger(TileResource.class);

  public static final String TILE_ENCODING = "gzip";

  public static final String TILE_TYPE = "application/vnd.mapbox-vector-tile";

  private final Supplier<TileStore> tileStoreSupplier;

  public TileResource(Supplier<TileStore> tileStoreSupplier) {
    this.tileStoreSupplier = tileStoreSupplier;
  }

  @Get("regex:^/tiles/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).mvt$")
  @Blocking
  public HttpResponse tile(@Param("z") int z, @Param("x") int x, @Param("y") int y) {
    TileCoord tileCoord = new TileCoord(x, y, z);
    try {
      TileStore tileStore = tileStoreSupplier.get();
      ByteBuffer blob = tileStore.read(tileCoord);
      if (blob != null) {
        var headers = ResponseHeaders.builder(200)
            .add(CONTENT_TYPE, TILE_TYPE)
            .add(CONTENT_ENCODING, TILE_ENCODING)
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
