package com.baremaps.openapi;

import com.baremaps.openapi.services.CollectionsService;
import com.baremaps.openapi.services.ConformanceService;
import com.baremaps.openapi.services.RootService;
import com.baremaps.openapi.services.StylesService;
import com.baremaps.openapi.services.TilesetsService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
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
    // Create a Postgresql datasource
    Jdbi jdbi = Jdbi.create("jdbc:postgresql://localhost:5432/baremaps?user=baremaps&password=baremaps")
        .installPlugin(new PostgresPlugin())
        .installPlugin(new Jackson2Plugin());

    // Configure ObjectMapper to skip None/NULL
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    jdbi.getConfig(Jackson2Config.class).setMapper(mapper);

    // Initialize the application
    ResourceConfig application = new ResourceConfig()
        .packages("org.glassfish.jersey.examples.jackson")
        .register(MyObjectMapperProvider.class)
        .register(JacksonFeature.class)
        .registerClasses(RootService.class,
            ConformanceService.class,
            CollectionsService.class,
            StylesService.class,
            TilesetsService.class)
        .register(new AbstractBinder() {
          @Override
          protected void configure() {
            bind(jdbi).to(Jdbi.class);
          }
        });

    // Create configurable starter for HTTP server.
    ServerContext serverContext = HttpServers.forPort(8080)
        .listenStreamingAndAwait(new HttpJerseyRouterBuilder()
            .buildStreaming(application));

    LOGGER.info("Listening on {}", serverContext.listenAddress());

    // Blocks and awaits shutdown of the server this ServerContext represents.
    serverContext.awaitShutdown();
  }

  @Provider
  private static class MyObjectMapperProvider implements ContextResolver<ObjectMapper> {

    final ObjectMapper defaultObjectMapper;

    public MyObjectMapperProvider() {
      defaultObjectMapper = createDefaultMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
      return defaultObjectMapper;
    }
  }

  private static ObjectMapper createDefaultMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);

    return mapper;
  }

}
