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

package com.baremaps.server.viewer;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.baremaps.model.MbStyle;
import com.baremaps.model.TileJSON;
import com.baremaps.tile.Tile;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.TileStoreException;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class ViewerResources {

  private final MbStyle style;

  private final TileJSON tileset;

  private final TileStore tileStore;

  @Inject
  public ViewerResources(MbStyle style, TileJSON tileset, TileStore tileStore) {
    this.style = style;
    this.tileset = tileset;
    this.tileStore = tileStore;
  }

  @GET
  @Path("style.json")
  @Produces(MediaType.APPLICATION_JSON)
  public MbStyle getStyle() {
    return style;
  }

  @GET
  @Path("tiles.json")
  @Produces(MediaType.APPLICATION_JSON)
  public TileJSON getTileset() {
    return tileset;
  }

  @GET
  @Path("/tiles/{z}/{x}/{y}.mvt")
  public Response getTile(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y) {
    Tile tile = new Tile(x, y, z);
    try {
      byte[] bytes = tileStore.read(tile);
      if (bytes != null) {
        return Response.status(200) // lgtm [java/xss]
            .header(CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
            .header(CONTENT_ENCODING, "gzip")
            .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            .entity(bytes)
            .build();
      } else {
        return Response.status(204).build();
      }
    } catch (TileStoreException ex) {
      return Response.status(404).build();
    }
  }

  @GET
  @javax.ws.rs.Path("{path:.*}")
  public Response get(@PathParam("path") String path) throws IOException {
    if (path.equals("") || path.endsWith("/")) {
      path += "index.html";
    }
    path = String.format("viewer/%s", path);
    var bytes = ClassLoader.getSystemClassLoader().getResourceAsStream(path).readAllBytes();
    return Response.ok().entity(bytes).build();
  }
}
