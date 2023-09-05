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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.vectortile.tilejson.TileJSON;
import org.apache.baremaps.vectortile.tilejsonextended.TileJSONExtended;
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
    assertEquals("aerialway", tileJSON.getVectorLayers().get(0).getId());
  }

  @Test
  public void validateTileset() throws IOException {
    // Mapping to a POJO for baremaps-core and baremaps-server
    var tileSet = objectMapper.readValue(resourceFile(tilesetFile), Tileset.class);
    // Mapping to a POJO strictly following TileJSON specifications for API clients.
    var tileJSON = objectMapper.readValue(resourceFile(tilesetFile), TileJSON.class);
    // Mapping to a POJO following TileJSON with extended fields specific to baremaps
    var tileJSONExtended =
        objectMapper.readValue(resourceFile(tilesetFile), TileJSONExtended.class);


    // Assert on Deserial
    assertEquals("jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        tileSet.getDatabase());
    assertEquals("aeroway", tileJSON.getVectorLayers().get(0).getId());
    assertEquals("aeroway", tileJSONExtended.getVectorLayers().get(0).getId());
    assertEquals("aeroway", tileJSONExtended.getVectorLayersExtended().get(0).getId());
    assertEquals("SELECT id, tags, geom FROM osm_nodes WHERE tags ? 'aeroway'",
        tileJSONExtended.getVectorLayersExtended().get(0).getQueries().get(0).getSql());


    var jsonTileSet = objectMapper.writeValueAsString(tileSet);
    var jsonTileJSON = objectMapper.writeValueAsString(tileJSON);
    var jsonTileJSONExtended = objectMapper.writeValueAsString(tileJSONExtended);

    // Password is only in internal POJO TileSet
    assertTrue(jsonTileSet.contains("password=baremaps"));
    assertFalse(jsonTileJSON.contains("password=baremaps"));
    assertFalse(jsonTileJSONExtended.contains("password=baremaps"));

    // Queries are only in internal TileSet and exposed TileJSONExtended
    assertTrue(jsonTileSet.contains("SELECT id"));
    assertFalse(jsonTileJSON.contains("SELECT id"));
    assertTrue(jsonTileJSONExtended.contains("SELECT id"));


    // Validating the Deserial/Serial/Deserial is equals to Deserial/Serial
    var tileJSONExtendedCopy = objectMapper.readValue(jsonTileJSONExtended, TileJSONExtended.class);
    assertEquals(objectMapper.writeValueAsString(tileJSONExtended),
        objectMapper.writeValueAsString(tileJSONExtendedCopy));
  }

  @Test
  public void validateSpecificationExample() throws IOException {
    // Mapping to a POJO for baremaps-core and baremaps-server
    var tileSet = objectMapper.readValue(resourceFile(referenceFile), Tileset.class);
    // Mapping to a POJO strictly following TileJSON specifications for API clients.
    var tileJSON = objectMapper.readValue(resourceFile(referenceFile), TileJSON.class);


    assertNull(tileSet.getDatabase());
    assertEquals("telephone", tileJSON.getVectorLayers().get(0).getId());
    assertEquals("the phone number", tileJSON.getVectorLayers().stream()
        .filter(vl -> vl.getId().equals("telephone"))
        .findFirst().get().getFields().get("phone_number"));
  }
}
