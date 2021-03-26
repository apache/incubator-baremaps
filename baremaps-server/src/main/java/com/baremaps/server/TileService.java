/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.server;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.baremaps.tile.Tile;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.TileStoreException;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileService {

  private static Logger logger = LoggerFactory.getLogger(TileService.class);

  private static final ResponseHeaders headers = ResponseHeaders.builder(200)
      .add(CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
      .add(CONTENT_ENCODING, "gzip")
      .add(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
      .build();

  private final TileStore tileStore;

  public TileService(TileStore tileStore) {
    this.tileStore = tileStore;
  }

  @Get("regex:^/(?<z>[0-9]+)/(?<x>[0-9]+)/(?<y>[0-9]+).pbf$")
  @Blocking
  public HttpResponse tile(@Param("z") int z, @Param("x") int x, @Param("y") int y) {
    Tile tile = new Tile(x, y, z);
    try {
      byte[] bytes = tileStore.read(tile);
      if (bytes != null) {
        HttpData data = HttpData.wrap(bytes);
        return HttpResponse.of(headers, data);
      } else {
        return HttpResponse.of(204);
      }
    } catch (TileStoreException ex) {
      logger.error(ex.getMessage());
      return HttpResponse.of(404);
    }
  }

}
