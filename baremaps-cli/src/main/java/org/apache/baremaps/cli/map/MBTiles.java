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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.apache.baremaps.cli.Options;
import org.apache.baremaps.server.CorsFilter;
import org.apache.baremaps.server.ServerResources;
import org.apache.baremaps.tilestore.TileCache;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.workflow.tasks.ExportVectorTiles;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "mbtiles", description = "Start a mbtiles server with caching capabilities.")
public class MBTiles implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(MBTiles.class);

  @Mixin
  private Options options;

  @Option(names = {"--cache"}, paramLabel = "CACHE", description = "The caffeine cache directive.")
  private String cache = "";

  @Option(names = {"--mbtiles"}, paramLabel = "MBTILES", description = "The mbtiles file.",
      required = true)
  private Path mbtiles;

  @Option(names = {"--tilejson"}, paramLabel = "TILEJSON", description = "The tileJSON file.",
      required = true)
  private Path tileset;

  @Option(names = {"--style"}, paramLabel = "STYLE", description = "The style file.",
      required = true)
  private Path style;

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    var objectMapper = objectMapper();
    var caffeineSpec = CaffeineSpec.parse(cache);
    var datasource = ExportVectorTiles.createDataSource(mbtiles);

    var tileStore = new org.apache.baremaps.tilestore.mbtiles.MBTiles(datasource);
    var tileCache = new TileCache(tileStore, caffeineSpec);

    // Configure the application
    var application =
        new ResourceConfig().register(CorsFilter.class).register(ServerResources.class)
            .register(newContextResolver(objectMapper)).register(new AbstractBinder() {
              @Override
              protected void configure() {
                bind(tileset).to(Path.class).named("tileset");
                bind(style).to(Path.class).named("style");
                bind(tileCache).to(TileStore.class);
                bind(objectMapper).to(ObjectMapper.class);
              }
            });

    var httpService = new HttpJerseyRouterBuilder().buildBlockingStreaming(application);
    var serverContext = HttpServers.forPort(port).listenBlockingStreamingAndAwait(httpService);

    logger.info("Listening on {}", serverContext.listenAddress());

    serverContext.awaitShutdown();
    return 0;
  }
}
