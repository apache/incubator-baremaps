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

package org.apache.baremaps.vectortile;


import static org.apache.baremaps.utils.ObjectMapperUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.vectortile.tilejson.TileJSON;
import org.apache.baremaps.vectortile.tileset.Tileset;
import org.junit.Test;

public class TileSetTest {

  private String tilesetFile = "/tilesets/tileset.json";
  private String referenceFile = "/tilesets/osm.json";
  private ObjectMapper objectMapper = objectMapper();
  ConfigReader configReader = new ConfigReader();

  private File resourceFile(String path) {
    return new File(TileSetTest.class.getResource(path).getFile());
  }

  @Test
  public void testBasemapJSConfig() throws IOException {
    var path = Path.of("../basemap/tileset.js");

    var tileSet = objectMapper.readValue(configReader.read(path), Tileset.class);
    var tileJSON = objectMapper.readValue(configReader.read(path), TileJSON.class);

    assertEquals("jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        tileSet.getDatabase());
    assertEquals("aerialway", tileJSON.getVectorLayers().get(0).id());
  }

  @Test
  public void validateTileset() throws IOException {
    // Mapping to a POJO for baremaps-core and baremaps-server
    var tileSet = objectMapper.readValue(resourceFile(tilesetFile), Tileset.class);
    // Mapping to a POJO strictly following TileJSON specifications for API clients.
    var tileJSON = objectMapper.readValue(resourceFile(tilesetFile), TileJSON.class);

    assertEquals("jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        tileSet.getDatabase());
    assertEquals("aeroway", tileJSON.getVectorLayers().get(0).id());
  }

  @Test
  public void validateSpecificationExample() throws IOException {
    // Mapping to a POJO for baremaps-core and baremaps-server
    var tileSet = objectMapper.readValue(resourceFile(referenceFile), Tileset.class);
    // Mapping to a POJO strictly following TileJSON specifications for API clients.
    var tileJSON = objectMapper.readValue(resourceFile(referenceFile), TileJSON.class);

    assertNull(tileSet.getDatabase());
    assertEquals("telephone", tileJSON.getVectorLayers().get(0).id());
    assertEquals("the phone number", tileJSON.getVectorLayers().stream()
        .filter(vl -> vl.id().equals("telephone"))
        .findFirst().get().fields().get("phone_number"));
  }
}
