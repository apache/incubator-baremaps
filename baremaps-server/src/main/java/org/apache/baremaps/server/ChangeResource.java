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


import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.*;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import org.apache.baremaps.config.ConfigReader;
import org.jvnet.hk2.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource that provides the changes in the tileset and style.
 */
@Singleton
@javax.ws.rs.Path("/")
public class ChangeResource {

  private static final Logger logger = LoggerFactory.getLogger(ChangeResource.class);

  private final ConfigReader configReader = new ConfigReader();

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final Path tileset;

  private final Path style;

  private final SseBroadcaster sseBroadcaster;

  private final OutboundSseEvent.Builder sseEventBuilder;

  /**
   * Constructs a {@code ChangeResource}.
   * 
   * @param tileset the path to the tileset
   * @param style the path to the style
   * @param sse the server-sent events
   */
  @Inject
  public ChangeResource(
      @Named("tileset") @Optional Path tileset,
      @Named("style") @Optional Path style,
      Sse sse) {
    this.tileset = tileset;
    this.style = style;
    this.sseBroadcaster = sse.newBroadcaster();
    this.sseEventBuilder = sse.newEventBuilder();
    var changeListener = new ChangeListener();
    new Thread(changeListener).start();
  }

  /**
   * Returns the changes in the tileset and style.
   * 
   * @param sseEventSink the server-sent events
   */
  @GET
  @javax.ws.rs.Path("changes")
  @Produces("text/event-stream")
  public void changes(@Context SseEventSink sseEventSink) {
    sseBroadcaster.register(sseEventSink);
  }

  /**
   * A {@code Runnable} that detects changes in the tileset and style.
   */
  class ChangeListener implements Runnable {

    /**
     * Detects changes in the tileset and style and broadcasts them.
     */
    @Override
    public void run() {
      try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
        if (tileset != null && Files.exists(tileset)) {
          registerRecursively(tileset.toAbsolutePath().getParent(), watchService);
        }
        if (style != null && Files.exists(style)) {
          registerRecursively(style.toAbsolutePath().getParent(), watchService);
        }
        WatchKey key;
        while ((key = watchService.take()) != null) {
          Path dir = (Path) key.watchable();

          for (WatchEvent<?> event : key.pollEvents()) {
            Path path = dir.resolve((Path) event.context()).toAbsolutePath();
            logger.info("Change detected in {}", path);

            if (style != null) {
              var value = configReader.read(style);
              var styleObjectNode = objectMapper.readValue(value, ObjectNode.class);

              // reload the page if changes affected the tileset
              if (tileset != null && path.endsWith(tileset.getFileName())) {
                styleObjectNode.put("reload", true);
              }

              // broadcast the changes
              sseBroadcaster.broadcast(sseEventBuilder.data(styleObjectNode.toString()).build());
            }
          }
          key.reset();
        }
      } catch (InterruptedException | IOException e) {
        logger.error(e.getMessage());
        Thread.currentThread().interrupt();
      }
    }

    private void registerRecursively(Path directory, WatchService watchService) throws IOException {
      Files.walk(directory)
          .filter(Files::isDirectory)
          .forEach(path -> {
            try {
              path.register(watchService, ENTRY_MODIFY);
            } catch (IOException e) {
              logger.error(e.getMessage());
            }
          });
    }


  }
}
