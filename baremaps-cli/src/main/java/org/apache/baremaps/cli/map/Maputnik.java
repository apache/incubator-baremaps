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

package org.apache.baremaps.cli.map;

import static io.servicetalk.data.jackson.jersey.ServiceTalkJacksonSerializerFeature.contextResolverFor;
import static org.apache.baremaps.server.DefaultObjectMapper.defaultObjectMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.apache.baremaps.database.PostgresUtils;
import org.apache.baremaps.server.CorsFilter;
import org.apache.baremaps.server.MaputnikResources;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "maputnik", description = "Start a maputnik editor.")
public class Maputnik implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Maputnik.class);

  @Option(names = {"--database"}, paramLabel = "DATABASE",
      description = "The JDBC url of Postgres.", required = true)
  private String database;

  @Option(names = {"--cache"}, paramLabel = "CACHE", description = "The caffeine cache directive.")
  private String cache = "";

  @Option(names = {"--tileset"}, paramLabel = "TILESET", description = "The tileset file.",
      required = true)
  private Path tileset;

  @Option(names = {"--style"}, paramLabel = "STYLE", description = "The style file.",
      required = true)
  private Path style;

  @Option(names = {"--host"}, paramLabel = "HOST", description = "The host of the server.")
  private String host = "localhost";

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    // Maputnik only supports json style files
    if (!style.endsWith(".json")) {
      logger.error("{} is not a JSON file", style);
      return 1;
    }

    // Create the data source
    try (var dataSource = PostgresUtils.dataSource(database)) {
      // Configure serialization
      var objectMapper = defaultObjectMapper();

      // Configure the application
      var application =
          new ResourceConfig().register(CorsFilter.class).register(MaputnikResources.class)
              .register(contextResolverFor(objectMapper)).register(new AbstractBinder() {
                @Override
                protected void configure() {
                  bind(tileset.toAbsolutePath()).to(Path.class).named("tileset");
                  bind(style.toAbsolutePath()).to(Path.class).named("style");
                  bind(dataSource).to(DataSource.class);
                  bind(objectMapper).to(ObjectMapper.class);
                }
              });

      // Start the server
      var httpService = new HttpJerseyRouterBuilder().buildBlockingStreaming(application);
      var serverContext = HttpServers.forPort(port).listenBlockingStreamingAndAwait(httpService);

      // Wait for the server to be closed
      logger.info("Listening on {}", serverContext.listenAddress());
      serverContext.awaitShutdown();
    }

    return 0;
  }
}
