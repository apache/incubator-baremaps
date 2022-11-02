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

import static org.apache.baremaps.server.DefaultObjectMapper.defaultObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.baremaps.cli.Options;
import org.apache.baremaps.style.Style;
import org.apache.baremaps.style.StyleSource;
import org.apache.baremaps.tileset.Tileset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "init", description = "Init configuration files.")
public class Init implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Init.class);

  @Mixin
  private Options options;

  @Option(names = {"--style"}, paramLabel = "STYLE", description = "A style file.")
  private Path style;

  @Option(names = {"--tileset"}, paramLabel = "TILESET", description = "A tileset file.")
  private Path tileset;

  @Override
  public Integer call() throws Exception {
    if (style != null) {
      Style styleObject = new Style();
      styleObject.setName("Baremaps");
      StyleSource sources = new StyleSource();
      sources.setType("vector");
      sources.setUrl("http://localhost:9000/tiles.json");
      styleObject.setSources(Map.of("baremaps", sources));
      Files.write(style,
          defaultObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(styleObject));
      logger.info("Style initialized: {}", style);
    }
    if (tileset != null) {
      Tileset tilesetObject = new Tileset();
      tilesetObject.setTilejson("2.2.0");
      tilesetObject.setName("Baremaps");
      tilesetObject.setTiles(Arrays.asList("http://localhost:9000/tiles.json"));
      Files.write(tileset,
          defaultObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(tilesetObject));
      logger.info("Tileset initialized: {}", tileset);
    }
    return 0;
  }
}
