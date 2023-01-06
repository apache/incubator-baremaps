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

package org.apache.baremaps.cli.geocoder;



import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.apache.baremaps.server.CorsFilter;
import org.apache.baremaps.server.GeocoderResources;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.MMapDirectory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "serve", description = "Start a tile server with caching capabilities.")
public class Serve implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Serve.class);

  @Option(
      names = {"--index"}, paramLabel = "INDEX", description = "The path to the lucene index.",
      required = true)
  private Path indexDirectory;

  @Option(names = {"--host"}, paramLabel = "HOST", description = "The host of the server.")
  private String host = "localhost";

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    try (
        var directory = MMapDirectory.open(indexDirectory);
        var searcherManager = new SearcherManager(directory, new SearcherFactory())) {
      // Configure the application
      var application = new ResourceConfig().register(CorsFilter.class)
          .register(GeocoderResources.class).register(new AbstractBinder() {
            @Override
            protected void configure() {
              bind(searcherManager).to(SearcherManager.class).named("searcherManager");
            }
          });

      var httpService = new HttpJerseyRouterBuilder().buildBlockingStreaming(application);
      var serverContext = HttpServers.forPort(port).listenBlockingStreamingAndAwait(httpService);

      logger.info("Listening on {}", serverContext.listenAddress());
      serverContext.awaitShutdown();
    }

    return 0;
  }
}
