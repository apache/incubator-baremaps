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

package org.apache.baremaps.ogcapi;

import static com.google.common.net.HttpHeaders.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.ogcapi.api.TilesApi;
import org.apache.baremaps.ogcapi.model.TileSetItem;
import org.apache.baremaps.ogcapi.model.TileSets;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;
import org.apache.baremaps.vectortile.tilejson.TileJSON;

@Singleton
public class TilesResource implements TilesApi {

  public static final String TILE_ENCODING = "gzip";

  public static final String TILE_TYPE = "application/vnd.mapbox-vector-tile";

  private final TileJSON tileJSON;

  private final TileStore tileStore;

  /**
   * Constructs a {@code StylesResource}.
   *
   * @param uriInfo
   * @param tileset
   * @param objectMapper
   * @throws IOException
   */
  @Inject
  public TilesResource(@Context UriInfo uriInfo, @Named("tileset") Path tilesetPath,
      ObjectMapper objectMapper, TileStore tileStore) throws IOException {
    this.tileJSON = objectMapper.readValue(new ConfigReader().read(tilesetPath), TileJSON.class);
    this.tileJSON.setTiles(List.of(uriInfo.getBaseUri().toString() + "tiles/default/{z}/{x}/{y}"));
    this.tileStore = tileStore;
  }

  /**
   * Get the tile sets.
   */
  @Override
  public Response getTileSets() {
    var tileSetItem = new TileSetItem();
    tileSetItem.setTitle("default");
    var tileSets = new TileSets();
    tileSets.setTileSets(List.of(tileSetItem));
    return Response.ok(tileSets).build();
  }

  /**
   * Get the tile set with the specified id.
   *
   * @param tileSetId the tile set id
   * @return the tile set
   */
  @Override
  public Response getTileSet(String tileSetId) {
    return Response.ok(tileJSON).build();
  }

  /**
   * Get the tile with the specified parameters.
   *
   * @param tileSetId the tile set id
   * @param tileMatrix the tile matrix
   * @param tileRow the tile row
   * @param tileCol the tile column
   * @return the tile
   */
  @Override
  public Response getTile(String tileSetId, String tileMatrix, Integer tileRow, Integer tileCol) {
    int z = Integer.parseInt(tileMatrix);
    int x = tileRow;
    int y = tileCol;
    TileCoord tileCoord = new TileCoord(x, y, z);
    try {
      ByteBuffer blob = tileStore.read(tileCoord);
      if (blob != null) {
        return Response.status(200) // lgtm [java/xss]
            .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            .header(CONTENT_TYPE, TILE_TYPE)
            .header(CONTENT_ENCODING, TILE_ENCODING)
            .entity(blob.array())
            .build();
      } else {
        return Response.status(204).build();
      }
    } catch (TileStoreException ex) {
      return Response.status(404).build();
    }
  }
}
