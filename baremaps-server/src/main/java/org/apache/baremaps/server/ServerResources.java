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

package org.apache.baremaps.server;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.vectortile.style.Style;
import org.apache.baremaps.vectortile.tilejson.TileJSON;

@Singleton
@javax.ws.rs.Path("/")
public class ServerResources {

  private final Style style;

  private final TileJSON tileJSON;

  @Inject
  public ServerResources(
      @Named("tileset") Path tileset,
      @Named("style") Path style,
      ObjectMapper objectMapper) throws IOException {
    ConfigReader configReader = new ConfigReader();
    this.style = objectMapper.readValue(configReader.read(style), Style.class);
    this.tileJSON = objectMapper.readValue(configReader.read(tileset), TileJSON.class);
  }

  @GET
  @javax.ws.rs.Path("style.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Style getStyle() {
    return style;
  }

  @GET
  @javax.ws.rs.Path("tiles.json")
  @Produces(MediaType.APPLICATION_JSON)
  public TileJSON getTileset() {
    return tileJSON;
  }
}
