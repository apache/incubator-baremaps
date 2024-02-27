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

package org.apache.baremaps.cli.tdtiles;



import static org.apache.baremaps.utils.ObjectMapperUtils.objectMapper;

import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.annotation.JacksonResponseConverterFunction;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.file.FileService;
import com.linecorp.armeria.server.file.HttpFile;
import java.util.concurrent.Callable;
import org.apache.baremaps.cli.Options;
import org.apache.baremaps.server.TdTilesResources;
import org.apache.baremaps.utils.PostgresUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "serve", description = "Start a 3d tile server.")
public class Serve implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Serve.class);

  @Mixin
  private Options options;

  @Option(names = {"--database"}, paramLabel = "DATABASE",
      description = "The JDBC url of Postgres.", required = true)
  private String database;

  @Option(names = {"--host"}, paramLabel = "HOST", description = "The host of the server.")
  private String host = "localhost";

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    var objectMapper = objectMapper();
    var datasource = PostgresUtils.createDataSource(database);

    var serverBuilder = Server.builder();
    serverBuilder.http(port);

    var jsonResponseConverter = new JacksonResponseConverterFunction(objectMapper);
    serverBuilder.annotatedService(new TdTilesResources(datasource), jsonResponseConverter);

    var index = HttpFile.of(ClassLoader.getSystemClassLoader(), "/tdtiles/index.html");
    serverBuilder.service("/", index.asService());
    serverBuilder.serviceUnder("/", FileService.of(ClassLoader.getSystemClassLoader(), "/tdtiles"));

    serverBuilder.decorator(CorsService.builderForAnyOrigin()
        .allowRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE,
            HttpMethod.OPTIONS, HttpMethod.HEAD)
        .allowRequestHeaders(HttpHeaderNames.ORIGIN, HttpHeaderNames.CONTENT_TYPE,
            HttpHeaderNames.ACCEPT, HttpHeaderNames.AUTHORIZATION)
        .allowCredentials()
        .exposeHeaders(HttpHeaderNames.LOCATION)
        .newDecorator());

    serverBuilder.disableServerHeader();
    serverBuilder.disableDateHeader();

    var server = serverBuilder.build();

    var startFuture = server.start();
    startFuture.join();

    var shutdownFuture = server.closeOnJvmShutdown();
    shutdownFuture.join();

    return 0;
  }
}
