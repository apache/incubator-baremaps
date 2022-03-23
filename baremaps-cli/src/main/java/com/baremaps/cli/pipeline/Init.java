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

package com.baremaps.cli.pipeline;

import static com.baremaps.server.utils.DefaultObjectMapper.defaultObjectMapper;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.blob.BlobStoreException;
import com.baremaps.cli.Options;
import com.baremaps.model.MbStyle;
import com.baremaps.model.MbStyleSources;
import com.baremaps.model.TileJSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "init", description = "Init the configuration files.")
public class Init implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Init.class);

  @Mixin private Options options;

  @Option(
      names = {"--tileset"},
      paramLabel = "TILESET",
      description = "The tileset file.")
  private URI tileset;

  @Option(
      names = {"--style"},
      paramLabel = "STYLE",
      description = "The style file.")
  private URI style;

  @Override
  public Integer call() throws BlobStoreException, IOException {
    BlobStore blobStore = options.blobStore();
    ObjectMapper mapper = defaultObjectMapper();

    if (style != null) {
      MbStyle styleObject = new MbStyle();
      styleObject.setName("Baremaps");
      MbStyleSources sources = new MbStyleSources();
      sources.setType("vector");
      sources.setUrl("http://localhost:9000/tiles.json");
      styleObject.setSources(Map.of("baremaps", sources));
      blobStore.put(
          style,
          Blob.builder()
              .withByteArray(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(styleObject))
              .build());
      logger.info("Style initialized: {}", style);
    }

    if (tileset != null) {
      TileJSON tilesetObject = new TileJSON();
      tilesetObject.setTilejson("2.2.0");
      tilesetObject.setName("Baremaps");
      tilesetObject.setTiles(Arrays.asList("http://localhost:9000/tiles.json"));
      blobStore.put(
          tileset,
          Blob.builder()
              .withByteArray(
                  mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(tilesetObject))
              .build());
      logger.info("Tileset initialized: {}", tileset);
    }

    return 0;
  }
}
