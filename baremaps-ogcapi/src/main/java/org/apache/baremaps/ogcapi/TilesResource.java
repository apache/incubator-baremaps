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

import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.baremaps.ogcapi.api.TilesApi;

@Singleton
public class TilesResource implements TilesApi {

  @Context
  UriInfo uriInfo;

  @Override
  public Response getTile(String tileSetId, String tileMatrix, Integer tileRow, Integer tileCol) {
    return null;
  }

  @Override
  public Response getTileSet(String tileSetId) {
    return null;
  }

  @Override
  public Response getTileSets() {
    return null;
  }
}
