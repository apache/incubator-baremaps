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

import static io.servicetalk.data.jackson.jersey.ServiceTalkJacksonSerializerFeature.newContextResolver;
import static org.apache.baremaps.utils.ObjectMapperUtils.objectMapper;

import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.apache.baremaps.cli.Options;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.server.*;
import org.apache.baremaps.server.CorsFilter;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.postgres.PostgresTileStore;
import org.apache.baremaps.utils.PostgresUtils;
import org.apache.baremaps.vectortile.style.Style;
import org.apache.baremaps.vectortile.tilejson.TileJSON;
import org.apache.baremaps.vectortile.tileset.Tileset;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "dev", description = "Start a development server with live reload.")
public class Dev implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Dev.class);

  @Mixin
  private Options options;

  @Option(names = {"--cache"}, paramLabel = "CACHE", description = "The caffeine cache directive.")
  private String cache = "";

  @Option(names = {"--tileset"}, paramLabel = "TILESET", description = "The tileset file.",
      required = true)
  private Path tilesetPath;

  @Option(names = {"--style"}, paramLabel = "STYLE", description = "The style file.",
      required = true)
  private Path stylePath;

  @Option(names = {"--host"}, paramLabel = "HOST", description = "The host of the server.")
  private String host = "localhost";

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    var configReader = new ConfigReader();
    var objectMapper = objectMapper();
    var tileset = objectMapper.readValue(configReader.read(this.tilesetPath), Tileset.class);
    var datasource = PostgresUtils.createDataSource(tileset.getDatabase());

    var tileStoreType = new TypeLiteral<Supplier<TileStore>>() {};
    var tileStoreSupplier = (Supplier<TileStore>) () -> {
      try {
        var tilesetObject =
            objectMapper.readValue(configReader.read(this.tilesetPath), Tileset.class);
        return new PostgresTileStore(datasource, tilesetObject);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    var styleSupplierType = new TypeLiteral<Supplier<Style>>() {};
    var styleSupplier = (Supplier<Style>) () -> {
      try {
        var config = configReader.read(stylePath);
        return objectMapper.readValue(config, Style.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    var tileJSONSupplierType = new TypeLiteral<Supplier<Tileset>>() {};
    var tileJSONSupplier = (Supplier<Tileset>) () -> {
      try {
        var config = configReader.read(tilesetPath);
        return objectMapper.readValue(config, Tileset.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    var application = new ResourceConfig()
        .register(CorsFilter.class)
        .register(ChangeResource.class)
        .register(TileResource.class)
        .register(StyleResource.class)
        .register(TilesetResource.class)
        .register(ChangeResource.class)
        .register(ClassPathResource.class)
        .register(newContextResolver(objectMapper))
        .register(new AbstractBinder() {
          @Override
          protected void configure() {
            bind("assets").to(String.class).named("directory");
            bind("viewer.html").to(String.class).named("index");
            bind(tilesetPath).to(Path.class).named("tileset");
            bind(stylePath).to(Path.class).named("style");
            bind(tileStoreSupplier).to(tileStoreType);
            bind(styleSupplier).to(styleSupplierType);
            bind(tileJSONSupplier).to(tileJSONSupplierType);
          }
        });

    var httpService = new HttpJerseyRouterBuilder().buildBlockingStreaming(application);
    var serverContext = HttpServers.forPort(port).listenBlockingStreamingAndAwait(httpService);

    logger.info("Listening on {}", serverContext.listenAddress());
    serverContext.awaitShutdown();

    return 0;
  }
}
