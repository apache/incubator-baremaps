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

import static com.google.common.net.HttpHeaders.*;

import java.nio.ByteBuffer;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.apache.baremaps.tilestore.TileCoord;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.TileStoreException;

@Singleton
@javax.ws.rs.Path("/")
public class TileResources {

  public static final String TILE_ENCODING = "gzip";

  public static final String TILE_TYPE = "application/vnd.mapbox-vector-tile";

  private final Supplier<TileStore> tileStoreSupplier;

  @Inject
  public TileResources(Supplier<TileStore> tileStoreSupplier) {
    this.tileStoreSupplier = tileStoreSupplier;
  }

  @GET
  @javax.ws.rs.Path("/tiles/{z}/{x}/{y}.mvt")
  public Response getTile(@PathParam("z") int z, @PathParam("x") int x, @PathParam("y") int y) {
    TileCoord tileCoord = new TileCoord(x, y, z);
    try {
      TileStore tileStore = tileStoreSupplier.get();
      ByteBuffer blob = tileStore.read(tileCoord);
      if (blob != null) {
        byte[] bytes = new byte[blob.remaining()];
        blob.get(bytes);
        return Response.status(200) // lgtm [java/xss]
            .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            .header(CONTENT_TYPE, TILE_TYPE)
            .header(CONTENT_ENCODING, TILE_ENCODING)
            .entity(bytes)
            .build();
      } else {
        return Response.status(204).build();
      }
    } catch (TileStoreException ex) {
      return Response.status(404).build();
    }
  }

}
