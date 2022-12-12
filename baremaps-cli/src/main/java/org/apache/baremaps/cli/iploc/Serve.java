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

package org.apache.baremaps.cli.iploc;



import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.apache.baremaps.iploc.database.InetnumLocationDao;
import org.apache.baremaps.iploc.database.InetnumLocationDaoSqliteImpl;
import org.apache.baremaps.server.CorsFilter;
import org.apache.baremaps.server.IplocResources;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "serve", description = "Start an IP to location web service.")
public class Serve implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Serve.class);

  @Option(names = {"--database"}, paramLabel = "DATABASE",
      description = "The path of the SQLite database.", defaultValue = "iploc.db")
  private Path database;

  @Option(names = {"--host"}, paramLabel = "HOST", description = "The host of the server.")
  private String host = "localhost";

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {

    String jdbcUrl = String.format("JDBC:sqlite:%s", database.toString());
    InetnumLocationDao inetnumLocationDao = new InetnumLocationDaoSqliteImpl(jdbcUrl);

    // Configure the application
    var application = new ResourceConfig().register(CorsFilter.class).register(IplocResources.class)
        .register(new AbstractBinder() {
          @Override
          protected void configure() {
            bind(inetnumLocationDao).to(InetnumLocationDao.class).named("inetnumLocationDao");
          }
        });

    var httpService = new HttpJerseyRouterBuilder().buildBlockingStreaming(application);
    var serverContext = HttpServers.forPort(port).listenBlockingStreamingAndAwait(httpService);

    logger.info("Listening on {}", serverContext.listenAddress());

    serverContext.awaitShutdown();
    return 0;
  }
}
