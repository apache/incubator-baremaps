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

package com.baremaps.cli.service;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.baremaps.tiles.TileStore;
import com.baremaps.util.tile.Tile;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.server.AbstractHttpService;
import com.linecorp.armeria.server.ServiceRequestContext;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TileService extends AbstractHttpService {

  private static Logger logger = LogManager.getLogger();

  private static final ResponseHeaders headers = ResponseHeaders.builder(200)
      .add(CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
      .add(CONTENT_ENCODING, "gzip")
      .add(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
      .build();

  private final TileStore tileStore;

  public TileService(TileStore tileStore) {
    this.tileStore = tileStore;
  }

  @Override
  protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) {
    return HttpResponse.from(CompletableFuture.supplyAsync(() -> {
      int z = Integer.parseInt(ctx.pathParam("z"));
      int x = Integer.parseInt(ctx.pathParam("x"));
      int y = Integer.parseInt(ctx.pathParam("y"));
      Tile tile = new Tile(x, y, z);
      try {
        byte[] bytes = tileStore.read(tile);
        if (bytes != null) {
          HttpData data = HttpData.wrap(bytes);
          return HttpResponse.of(headers, data);
        } else {
          return HttpResponse.of(204);
        }
      } catch (IOException ex) {
        return HttpResponse.of(404);
      }
    }, ctx.blockingTaskExecutor()));
  }

}
