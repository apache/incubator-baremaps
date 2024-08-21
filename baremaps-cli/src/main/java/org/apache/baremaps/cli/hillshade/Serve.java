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

package org.apache.baremaps.cli.hillshade;

import static org.apache.baremaps.utils.ObjectMapperUtils.objectMapper;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.linecorp.armeria.common.*;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.annotation.JacksonResponseConverterFunction;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.file.HttpFile;
import java.util.concurrent.Callable;
import org.apache.baremaps.raster.ElevationUtils;
import org.apache.baremaps.server.BufferedImageResource;
import org.apache.baremaps.server.VectorTileResource;
import org.apache.baremaps.tilestore.raster.*;
import org.apache.baremaps.tilestore.raster.RasterTileCache;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "serve", description = "Start a tile server that computes hillshades.")
public class Serve implements Callable<Integer> {

  @Option(names = {"--host"}, paramLabel = "HOST", description = "The host of the server.")
  private String host = "localhost";

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {

    var serverBuilder = Server.builder();
    serverBuilder.http(port);

    var objectMapper = objectMapper();
    var jsonResponseConverter = new JacksonResponseConverterFunction(objectMapper);

    var elevationTileStore =
        new RasterTileCache(new RasterElevationTileStore(), CaffeineSpec.parse("maximumSize=1000"));
    var rasterHillshadeTileStore =
        new RasterHillshadeTileStore(elevationTileStore, ElevationUtils::pixelToElevationTerrarium);
    var vectorHillshadeTileStore = new VectorHillshadeTileStore(elevationTileStore,
        ElevationUtils::pixelToElevationTerrarium);
    var contourTileStore = new VectorContourTileStore(elevationTileStore);


    serverBuilder.annotatedService(
        "/raster",
        new BufferedImageResource(() -> elevationTileStore),
        jsonResponseConverter);

    serverBuilder.annotatedService(
        "/contour",
        new VectorTileResource(() -> contourTileStore),
        jsonResponseConverter);

    serverBuilder.annotatedService(
        "/tiles",
        new VectorTileResource(() -> vectorHillshadeTileStore),
        jsonResponseConverter);

    var index = HttpFile.of(ClassLoader.getSystemClassLoader(), "/raster/hillshade.html");
    serverBuilder.service("/", index.asService());

    serverBuilder.decorator(CorsService.builderForAnyOrigin()
        .allowAllRequestHeaders(true)
        .allowRequestMethods(
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.DELETE,
            HttpMethod.OPTIONS,
            HttpMethod.HEAD)
        .allowCredentials()
        .exposeHeaders(HttpHeaderNames.LOCATION)
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
