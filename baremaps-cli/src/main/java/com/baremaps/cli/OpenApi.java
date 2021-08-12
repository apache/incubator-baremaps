
package com.baremaps.cli;

import com.baremaps.openapi.BaremapsServer;
import com.baremaps.openapi.services.*;
import com.baremaps.postgres.jdbc.PostgresUtils;
import com.baremaps.server.CorsFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.servicetalk.http.api.BlockingStreamingHttpService;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.sql.DataSource;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.util.concurrent.Callable;

@Command(name = "openapi", description = "Serve the openapi API.")
public class OpenApi implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(OpenApi.class);

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the Postgres database.",
      required = true)
  private String database;

  @Option(
      names = {"--port"},
      paramLabel = "PORT",
      description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    DataSource datasource = PostgresUtils.datasource(this.database);

    Jdbi jdbi = Jdbi.create(datasource)
            .installPlugin(new PostgresPlugin())
            .installPlugin(new Jackson2Plugin());

    // Configure ObjectMapper to skip None/NULL
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    jdbi.getConfig(Jackson2Config.class).setMapper(mapper);

    // Initialize the application
    ResourceConfig application = new ResourceConfig()
            .packages("org.glassfish.jersey.examples.jackson")
            .registerClasses(MyObjectMapperProvider.class,
                    JacksonFeature.class,
                    RootService.class,
                    CorsFilter.class,
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

    BlockingStreamingHttpService httpService = new HttpJerseyRouterBuilder()
        .buildBlockingStreaming(application);
    ServerContext serverContext = HttpServers.forPort(port)
        .listenBlockingStreamingAndAwait(httpService);

    logger.info("Listening on {}", serverContext.listenAddress());

    serverContext.awaitShutdown();

    return 0;
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
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    return mapper;
  }

}