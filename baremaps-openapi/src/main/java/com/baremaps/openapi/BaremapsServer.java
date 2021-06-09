package com.baremaps.openapi;

import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaremapsServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaremapsServer.class);

  private BaremapsServer() {
    // No instances.
  }

  /**
   * Starts this server.
   *
   * @param args Program arguments, none supported yet.
   * @throws Exception If the server could not be started.
   */
  public static void main(String[] args) throws Exception {
    // Create configurable starter for HTTP server.
    ServerContext serverContext = HttpServers.forPort(8080)
        .listenStreamingAndAwait(new HttpJerseyRouterBuilder()
            .buildStreaming(new BaremapsApplication()));

    LOGGER.info("Listening on {}", serverContext.listenAddress());

    // Blocks and awaits shutdown of the server this ServerContext represents.
    serverContext.awaitShutdown();
  }

}
