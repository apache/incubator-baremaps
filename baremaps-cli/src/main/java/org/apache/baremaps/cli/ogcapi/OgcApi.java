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

package org.apache.baremaps.cli.ogcapi;

import static io.servicetalk.data.jackson.jersey.ServiceTalkJacksonSerializerFeature.contextResolverFor;
import static org.apache.baremaps.server.DefaultObjectMapper.defaultObjectMapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.servicetalk.http.api.BlockingStreamingHttpService;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import io.servicetalk.transport.api.ServerContext;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.apache.baremaps.cli.Options;
import org.apache.baremaps.database.PostgresUtils;
import org.apache.baremaps.ogcapi.ApiResource;
import org.apache.baremaps.ogcapi.CollectionsResource;
import org.apache.baremaps.ogcapi.ConformanceResource;
import org.apache.baremaps.ogcapi.ImportResource;
import org.apache.baremaps.ogcapi.RootResource;
import org.apache.baremaps.ogcapi.StudioResource;
import org.apache.baremaps.ogcapi.StylesResource;
import org.apache.baremaps.ogcapi.SwaggerResource;
import org.apache.baremaps.ogcapi.TilesetsResource;
import org.apache.baremaps.server.CorsFilter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "ogcapi", description = "OGC API server (experimental).")
public class OgcApi implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(OgcApi.class);

  @Mixin
  private Options options;

  @Option(names = {"--database"}, paramLabel = "DATABASE",
      description = "The JDBC url of the Postgres database.", required = true)
  private String database;

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    // Configure serialization
    ObjectMapper mapper = defaultObjectMapper();
    mapper.readValue("", JsonNode.class);

    // Configure jdbi and set the ObjectMapper
    DataSource datasource = PostgresUtils.dataSource(this.database);
    Jdbi jdbi = Jdbi.create(datasource).installPlugin(new PostgresPlugin())
        .installPlugin(new Jackson2Plugin())
        .configure(Jackson2Config.class, config -> config.setMapper(mapper));

    // Initialize the application
    ResourceConfig application = new ResourceConfig()
        .registerClasses(SwaggerResource.class, RootResource.class, CorsFilter.class,
            ConformanceResource.class, CollectionsResource.class, StylesResource.class,
            TilesetsResource.class, StudioResource.class, ImportResource.class,
            MultiPartFeature.class)
        .register(new ApiResource("studio-openapi.yaml")).register(contextResolverFor(mapper))
        .register(new AbstractBinder() {
          @Override
          protected void configure() {
            bind(datasource).to(DataSource.class);
            bind(jdbi).to(Jdbi.class);
          }
        });

    BlockingStreamingHttpService httpService =
        new HttpJerseyRouterBuilder().buildBlockingStreaming(application);
    ServerContext serverContext =
        HttpServers.forPort(port).listenBlockingStreamingAndAwait(httpService);

    logger.info("Listening on {}", serverContext.listenAddress());

    serverContext.awaitShutdown();

    return 0;
  }
}
