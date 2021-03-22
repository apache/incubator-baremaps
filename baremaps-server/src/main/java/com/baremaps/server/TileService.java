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
import freemarker.template.TemplateException;
import io.micronaut.core.annotation.Blocking;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/tiles")
public class TileService {

  private static Logger logger = LoggerFactory.getLogger(TileService.class);

  private final TileStore tileStore;

  public TileService(TileStore tileStore) {
    this.tileStore = tileStore;
  }

  @Blocking
  @Get("/{z}/{x}/{y}.pbf")
  public HttpResponse render(int z, int x, int y) throws IOException, TemplateException {
    Tile tile = new Tile(x, y, z);
    try {
      byte[] bytes = tileStore.read(tile);
      if (bytes != null) {
        return HttpResponse.status(HttpStatus.OK)
            .header(CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
            .header(CONTENT_ENCODING, "gzip")
            .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            .body(bytes);
      } else {
        return HttpResponse.status(HttpStatus.NO_CONTENT);
      }
    } catch (TileStoreException ex) {
      logger.error(ex.getMessage());
      return HttpResponse.status(HttpStatus.NOT_FOUND);
    }
  }

}
