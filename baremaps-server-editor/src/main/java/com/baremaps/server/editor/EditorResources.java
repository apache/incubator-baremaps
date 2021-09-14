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

package com.baremaps.server.editor;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.blob.BlobStoreException;
import com.baremaps.model.MbStyle;
import com.baremaps.model.TileJSON;
import com.baremaps.tile.Tile;
import com.baremaps.tile.TileStore;
import com.baremaps.tile.postgres.PostgresQuery;
import com.baremaps.tile.postgres.PostgresTileStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@javax.ws.rs.Path("/")
public class EditorResources {

  private static final Logger logger = LoggerFactory.getLogger(EditorResources.class);

  private final URI style;

  private final URI tileset;

  private final BlobStore blobStore;

  private final DataSource dataSource;

  private final ObjectMapper objectMapper;

  private final SseBroadcaster sseBroadcaster;

  private final Thread fileWatcher;

  @Inject
  public EditorResources(
      Configuration configuration,
      BlobStore blobStore,
      DataSource dataSource,
      ObjectMapper objectMapper,
      Sse sse) {
    this.tileset = URI.create(configuration.getProperty("baremaps.tileset").toString());
    this.style = URI.create(configuration.getProperty("baremaps.style").toString());
    this.blobStore = blobStore;
    this.dataSource = dataSource;
    this.objectMapper = objectMapper;

    // Observe the file system for changes
    OutboundSseEvent.Builder sseEventBuilder = sse.newEventBuilder();
    this.sseBroadcaster = sse.newBroadcaster();
    fileWatcher =
        new Thread(
            () -> {
              try {
                Path tilesetFile = Paths.get(tileset.getPath()).toAbsolutePath();
                Path styleFile = Paths.get(style.getPath()).toAbsolutePath();
                WatchService watchService = FileSystems.getDefault().newWatchService();
                tilesetFile.getParent().register(watchService, ENTRY_MODIFY);
                styleFile.getParent().register(watchService, ENTRY_MODIFY);
                WatchKey key;
                while ((key = watchService.take()) != null) {
                  Path dir = (Path) key.watchable();
                  for (WatchEvent<?> event : key.pollEvents()) {
                    Path path = dir.resolve((Path) event.context());
                    InputStream inputStream = blobStore.get(style).getInputStream();
                    ObjectNode jsonNode = objectMapper.readValue(inputStream, ObjectNode.class);
                    jsonNode.put("reload", path.endsWith(tilesetFile.getFileName()));
                    sseBroadcaster.broadcast(sseEventBuilder.data(jsonNode.toString()).build());
                  }
                  key.reset();
                }
              } catch (InterruptedException e) {
                logger.error(e.getMessage());
                Thread.currentThread().interrupt();
              } catch (BlobStoreException | IOException e) {
                logger.error(e.getMessage());
              }
            });
    fileWatcher.start();
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
      List<PostgresQuery> queries =
          getTileset().getVectorLayers().stream()
              .flatMap(
                  layer ->
                      layer.getQueries().stream()
                          .map(
                              query ->
                                  new PostgresQuery(
                                      layer.getId(),
                                      query.getMinzoom(),
                                      query.getMaxzoom(),
                                      query.getSql())))
              .collect(Collectors.toList());
      TileStore tileStore = new PostgresTileStore(dataSource, queries);
      Tile tile = new Tile(x, y, z);
      byte[] bytes = tileStore.read(tile);
      if (bytes != null) {
        return Response.status(200)
            .header(CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
            .header(CONTENT_ENCODING, "gzip")
            .entity(bytes)
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
    path = String.format("maputnik/%s", path);
    var bytes = ClassLoader.getSystemClassLoader().getResourceAsStream(path).readAllBytes();
    return Response.ok().entity(bytes).build();
  }
}
