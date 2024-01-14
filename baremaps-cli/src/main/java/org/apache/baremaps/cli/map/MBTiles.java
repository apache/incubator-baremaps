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

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.annotation.JacksonResponseConverterFunction;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.file.FileService;
import com.linecorp.armeria.server.file.HttpFile;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.apache.baremaps.cli.Options;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.server.*;
import org.apache.baremaps.tilestore.TileCache;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.mbtiles.MBTilesStore;
import org.apache.baremaps.utils.SqliteUtils;
import org.apache.baremaps.vectortile.style.Style;
import org.apache.baremaps.vectortile.tilejson.TileJSON;
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
  private Path mbtilesPath;

  @Option(names = {"--tilejson"}, paramLabel = "TILEJSON", description = "The tileJSON file.",
      required = true)
  private Path tileJSONPath;

  @Option(names = {"--style"}, paramLabel = "STYLE", description = "The style file.",
      required = true)
  private Path stylePath;

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    var objectMapper = objectMapper();
    var configReader = new ConfigReader();
    var caffeineSpec = CaffeineSpec.parse(cache);

    var datasource = SqliteUtils.createDataSource(mbtilesPath, true);
    var tileStore = new MBTilesStore(datasource);
    var tileCache = new TileCache(tileStore, caffeineSpec);
    var tileStoreSupplier = (Supplier<TileStore>) () -> tileCache;

    var style = objectMapper.readValue(configReader.read(stylePath), Style.class);
    var styleSupplier = (Supplier<Style>) () -> style;

    var tileJSON = objectMapper.readValue(configReader.read(tileJSONPath), TileJSON.class);
    var tileJSONSupplier = (Supplier<TileJSON>) () -> tileJSON;

    var serverBuilder = Server.builder();
    serverBuilder.http(port);

    JacksonResponseConverterFunction jsonResponseConverter =
        new JacksonResponseConverterFunction(objectMapper);
    serverBuilder.annotatedService(new TileResource(tileStoreSupplier), jsonResponseConverter);
    serverBuilder.annotatedService(new StyleResource(styleSupplier), jsonResponseConverter);
    serverBuilder.annotatedService(new TileJSONResource(tileJSONSupplier), jsonResponseConverter);

    HttpFile index = HttpFile.of(ClassLoader.getSystemClassLoader(), "/assets/server.html");
    serverBuilder.service("/", index.asService());
    serverBuilder.serviceUnder("/", FileService.of(ClassLoader.getSystemClassLoader(), "/assets"));

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

    Server server = serverBuilder.build();
    CompletableFuture<Void> future = server.start();
    future.join();

    return 0;
  }
}
