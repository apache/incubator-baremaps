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

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

import com.baremaps.core.blob.Blob;
import com.baremaps.core.blob.BlobStore;
import com.baremaps.core.blob.BlobStoreException;
import com.baremaps.core.tile.PostgresQuery;
import com.baremaps.core.tile.PostgresTileStore;
import com.baremaps.core.tile.Tile;
import com.baremaps.core.tile.TileStore;
import com.baremaps.model.MbStyle;
import com.baremaps.model.TileJSON;
import com.baremaps.server.ogcapi.Conversions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@javax.ws.rs.Path("/")
public class DevelopmentResources {

  private static final Logger logger = LoggerFactory.getLogger(DevelopmentResources.class);

  private final String assets;

  private final URI style;

  private final URI tileset;

  private final BlobStore blobStore;

  private final DataSource dataSource;

  private final ObjectMapper objectMapper;

  private final Sse sse;
  private final SseBroadcaster sseBroadcaster;
  private final OutboundSseEvent.Builder sseEventBuilder;

  @Inject
  public DevelopmentResources(
      @Named("assets") String assets,
      @Named("tileset") URI tileset,
      @Named("style") URI style,
      BlobStore blobStore,
      DataSource dataSource,
      ObjectMapper objectMapper,
      Sse sse) {
    this.assets = assets;
    this.tileset = tileset;
    this.style = style;
    this.blobStore = blobStore;
    this.dataSource = dataSource;
    this.objectMapper = objectMapper;
    this.sse = sse;
    this.sseBroadcaster = sse.newBroadcaster();
    this.sseEventBuilder = sse.newEventBuilder();

    // Observe the file system for changes
    Set<Path> directories = new HashSet<>(Arrays.asList(
        Paths.get(tileset.getPath()).toAbsolutePath().getParent(),
        Paths.get(style.getPath()).toAbsolutePath().getParent()));
    new Thread(new DirectoryWatcher(directories, this::broadcastChanges)).start();
  }

  public void broadcastChanges(Path path) {
    try (InputStream styleInputStream = blobStore.get(style).getInputStream()) {
      var styleObjectNode = objectMapper.readValue(styleInputStream, ObjectNode.class);

      // reload the page if changes affected the tileset
      var tilesetPath = Paths.get(tileset.getPath()).toAbsolutePath();
      styleObjectNode.put("reload", path.endsWith(tilesetPath.getFileName()));

      OutboundSseEvent.Builder sseEventBuilder = sse.newEventBuilder();
      sseBroadcaster.broadcast(sseEventBuilder.data(styleObjectNode.toString()).build());
    } catch (IOException e) {
      logger.error(e.getMessage());
    } catch (BlobStoreException e) {
      logger.error(e.getMessage());
    }
  }


  @GET
  @javax.ws.rs.Path("changes")
  @Produces("text/event-stream")
  public void changes(@Context SseEventSink sseEventSink) {
    sseBroadcaster.register(sseEventSink);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @javax.ws.rs.Path("style.json")
  public void putStyle(MbStyle json) throws JsonProcessingException, BlobStoreException {
    byte[] value = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(json);
    blobStore.put(style, Blob.builder().withByteArray(value).build());
  }

  @PUT
  @javax.ws.rs.Path("tiles.json")
  public void putTiles(JsonNode json) throws JsonProcessingException, BlobStoreException {
    byte[] value = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(json);
    blobStore.put(tileset, Blob.builder().withByteArray(value).build());
  }

  @GET
  @javax.ws.rs.Path("style.json")
  @Produces(MediaType.APPLICATION_JSON)
  public MbStyle getStyle() throws BlobStoreException, IOException {
    try (InputStream inputStream = blobStore.get(style).getInputStream()) {
      return objectMapper.readValue(inputStream, MbStyle.class);
    }
  }

  @GET
  @javax.ws.rs.Path("tiles.json")
  @Produces(MediaType.APPLICATION_JSON)
  public TileJSON getTileset() throws BlobStoreException, IOException {
    try (InputStream inputStream = blobStore.get(tileset).getInputStream()) {
      return objectMapper.readValue(inputStream, TileJSON.class);
    }
  }

  @GET
  @javax.ws.rs.Path("/tiles/{z}/{x}/{y}.mvt")
  public Response getTile(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y) {
    try {
      List<PostgresQuery> queries = Conversions.asPostgresQuery(getTileset());
      TileStore tileStore = new PostgresTileStore(dataSource, queries);
      Tile tile = new Tile(x, y, z);
      Blob blob = tileStore.read(tile);
      if (blob != null) {
        return Response.status(200)
            .header(CONTENT_TYPE, blob.getContentType())
            .header(CONTENT_ENCODING, blob.getContentEncoding())
            .entity(blob.getInputStream())
            .build();
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
    path = String.format("%s/%s", assets, path);
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
      var bytes = inputStream.readAllBytes();
      return Response.ok().entity(bytes).build();
    } catch (IOException e) {
      return Response.status(404).build();
    }
  }
}
