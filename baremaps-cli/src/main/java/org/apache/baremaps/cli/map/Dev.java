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

package org.apache.baremaps.cli.map;

import static org.apache.baremaps.utils.ObjectMapperUtils.objectMapper;

import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.annotation.JacksonResponseConverterFunction;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.file.FileService;
import com.linecorp.armeria.server.file.HttpFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.apache.baremaps.cli.Options;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.maplibre.style.Style;
import org.apache.baremaps.maplibre.tileset.Tileset;
import org.apache.baremaps.server.ChangeResource;
import org.apache.baremaps.server.StyleResource;
import org.apache.baremaps.server.TileResource;
import org.apache.baremaps.server.TilesetResource;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.postgres.PostgresTileStore;
import org.apache.baremaps.utils.PostgresUtils;
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

  @Option(names = {"--assets"}, paramLabel = "ASSETS", description = "The assets directory.",
      required = false)
  private Path assetsPath;

  @Option(names = {"--host"}, paramLabel = "HOST", description = "The host of the server.")
  private String host = "localhost";

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    var configReader = new ConfigReader();
    var objectMapper = objectMapper();
    var tileset = objectMapper.readValue(configReader.read(this.tilesetPath), Tileset.class);
    var datasource = PostgresUtils.createDataSourceFromObject(tileset.getDatabase());

    var tilesetSupplier = (Supplier<Tileset>) () -> {
      try {
        var config = configReader.read(tilesetPath);
        return objectMapper.readValue(config, Tileset.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    var tileStoreSupplier = (Supplier<TileStore>) () -> {
      var tileJSON = tilesetSupplier.get();
      return new PostgresTileStore(datasource, tileJSON);
    };

    var styleSupplier = (Supplier<Style>) () -> {
      try {
        var config = configReader.read(stylePath);
        return objectMapper.readValue(config, Style.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    var serverBuilder = Server.builder();
    serverBuilder.http(port);

    var jsonResponseConverter = new JacksonResponseConverterFunction(objectMapper);
    serverBuilder.annotatedService(new ChangeResource(tilesetPath, stylePath),
        jsonResponseConverter);
    serverBuilder.annotatedService(new TileResource(tileStoreSupplier), jsonResponseConverter);
    serverBuilder.annotatedService(new StyleResource(styleSupplier), jsonResponseConverter);
    serverBuilder.annotatedService(new TilesetResource(tilesetSupplier), jsonResponseConverter);

    var index = HttpFile.of(ClassLoader.getSystemClassLoader(), "/static/viewer.html");
    serverBuilder.service("/", index.asService());
    serverBuilder.serviceUnder("/", FileService.of(ClassLoader.getSystemClassLoader(), "/static"));

    if (assetsPath != null) {
      serverBuilder.serviceUnder("/assets", FileService.of(assetsPath));
    }

    serverBuilder.decorator(CorsService.builderForAnyOrigin()
        .allowRequestMethods(HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT)
        .allowRequestHeaders("Origin", "Content-Type", "Accept")
        .newDecorator());

    serverBuilder.serviceUnder("/docs", new DocService());


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
