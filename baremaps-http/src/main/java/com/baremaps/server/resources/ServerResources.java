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

package com.baremaps.server.resources;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStoreException;
import com.baremaps.blob.ConfigBlobStore;
import com.baremaps.core.tile.Tile;
import com.baremaps.core.tile.TileStore;
import com.baremaps.core.tile.TileStoreException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
@Path("/")
public class ServerResources {

  private final URI style;

  private final URI tileset;

  private final ConfigBlobStore blobStore;

  private final TileStore tileStore;

  @Inject
  public ServerResources(
      @Named("tileset") URI tileset,
      @Named("style") URI style,
      ConfigBlobStore blobStore,
      TileStore tileStore)
      throws BlobStoreException, IOException {
    this.tileset = tileset;
    this.style = style;
    this.blobStore = blobStore;
    this.tileStore = tileStore;
  }

  @GET
  @Path("style.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStyle() throws BlobStoreException, IOException {
    return Response.ok(blobStore.get(style).getInputStream().readAllBytes()).build();
  }

  @GET
  @Path("tiles.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTileset() throws BlobStoreException, IOException {
    return Response.ok(blobStore.get(tileset).getInputStream().readAllBytes()).build();
  }

  @GET
  @Path("/tiles/{z}/{x}/{y}.mvt")
  public Response getTile(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y) {
    Tile tile = new Tile(x, y, z);
    try {
      Blob blob = tileStore.read(tile);
      if (blob != null) {
        return Response.status(200) // lgtm [java/xss]
            .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            .header(CONTENT_TYPE, blob.getContentType())
            .header(CONTENT_ENCODING, blob.getContentEncoding())
            .entity(blob.getInputStream())
            .build();
      } else {
        return Response.status(204).build();
      }
    } catch (TileStoreException ex) {
      return Response.status(404).build();
    }
  }

  @GET
  @Path("{path:.*}")
  public Response get(@PathParam("path") String path) throws IOException {
    if (path.equals("") || path.endsWith("/")) {
      path += "index.html";
    }
    path = String.format("server/%s", path);
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
      var bytes = inputStream.readAllBytes();
      return Response.ok().entity(bytes).build();
    } catch (IOException e) {
      return Response.status(404).build();
    }
  }
}
