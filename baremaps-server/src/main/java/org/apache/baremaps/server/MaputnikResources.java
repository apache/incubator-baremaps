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

package org.apache.baremaps.server;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.Sse;
import org.apache.baremaps.database.tile.PostgresTileStore;
import org.apache.baremaps.database.tile.Tile;
import org.apache.baremaps.database.tile.TileStore;
import org.apache.baremaps.style.Style;
import org.apache.baremaps.tileset.Tileset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@javax.ws.rs.Path("/")
public class MaputnikResources {

  private static final Logger logger = LoggerFactory.getLogger(MaputnikResources.class);

  private final Path style;

  private final Path tileset;

  private final DataSource dataSource;

  private final ObjectMapper objectMapper;

  public static final String TILE_ENCODING = "gzip";

  public static final String TILE_TYPE = "application/vnd.mapbox-vector-tile";

  @Inject
  public MaputnikResources(@Named("tileset") Path tileset, @Named("style") Path style,
      DataSource dataSource, ObjectMapper objectMapper, Sse sse) {
    this.tileset = tileset.toAbsolutePath();
    this.style = style.toAbsolutePath();
    this.dataSource = dataSource;
    this.objectMapper = objectMapper;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @javax.ws.rs.Path("style.json")
  public void putStyle(JsonNode json) throws IOException {
    byte[] value = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(json);
    Files.write(style, value);
  }

  @PUT
  @javax.ws.rs.Path("tiles.json")
  public void putTiles(JsonNode json) throws IOException {
    byte[] value = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(json);
    Files.write(tileset, value);
  }

  @GET
  @javax.ws.rs.Path("style.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Style getStyle() throws IOException {
    return objectMapper.readValue(style.toFile(), Style.class);
  }

  @GET
  @javax.ws.rs.Path("tiles.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Tileset getTileset() throws IOException {
    return objectMapper.readValue(tileset.toFile(), Tileset.class);
  }

  @GET
  @javax.ws.rs.Path("/tiles/{z}/{x}/{y}.mvt")
  public Response getTile(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y) {
    try {
      TileStore tileStore = new PostgresTileStore(dataSource, getTileset());
      Tile tile = new Tile(x, y, z);
      ByteBuffer blob = tileStore.read(tile);
      if (blob != null) {
        return Response.status(200).header(CONTENT_TYPE, TILE_TYPE)
            .header(CONTENT_ENCODING, TILE_ENCODING).entity(blob.array()).build();
      } else {
        return Response.status(204).build();
      }
    } catch (Exception ex) {
      logger.error("Tile error", ex);
      return Response.status(404).build();
    }
  }

  @GET
  @javax.ws.rs.Path("{path:.*}")
  public Response get(@PathParam("path") String path) throws IOException {
    if (path.equals("") || path.endsWith("/")) {
      path += "index.html";
    }
    path = String.format("maputnik/%s", path);
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
      var bytes = inputStream.readAllBytes();
      return Response.ok().entity(bytes).build();
    } catch (IOException e) {
      return Response.status(404).build();
    }
  }
}
