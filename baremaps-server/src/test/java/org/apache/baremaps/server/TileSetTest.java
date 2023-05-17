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


import static org.apache.baremaps.utils.ObjectMapperUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import org.apache.baremaps.server.tilejson.TileJSON;
import org.apache.baremaps.vectortile.tileset.Tileset;
import org.junit.Test;

public class TileSetTest {

  String tilesetFile = "/tilesets/tileset.json";
  String referenceFile = "/tilesets/osm.json";

  private File resourceFile(String path) {
    return new File(getClass().getResource(path).getFile());
  }

  @Test
  public void validateTileset() throws IOException {
    var objectMapper = objectMapper();
    // Mapping to a POJO for baremaps-core and baremaps-server
    var tileSet = objectMapper.readValue(resourceFile(tilesetFile), Tileset.class);
    // Mapping to a POJO strictly following TileJSON specifications for API clients.
    var tileJSON = objectMapper.readValue(resourceFile(tilesetFile), TileJSON.class);

    assertEquals(tileSet.getDatabase(),
        "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps");
    assertEquals(tileJSON.vectorLayers().get(0).id(), "aeroway");
  }

  @Test
  public void validateSpecificationExample() throws IOException {
    var objectMapper = objectMapper();
    // Mapping to a POJO for baremaps-core and baremaps-server
    var tileSet = objectMapper.readValue(resourceFile(referenceFile), Tileset.class);
    // Mapping to a POJO strictly following TileJSON specifications for API clients.
    var tileJSON = objectMapper.readValue(resourceFile(referenceFile), TileJSON.class);

    assertNull(tileSet.getDatabase());
    assertEquals(tileJSON.vectorLayers().get(0).id(), "telephone");
  }
}
