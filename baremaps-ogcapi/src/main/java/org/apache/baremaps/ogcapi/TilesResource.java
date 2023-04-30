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

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import org.apache.baremaps.ogcapi.api.TilesApi;
import org.apache.baremaps.ogcapi.model.AllCollections;
import org.apache.baremaps.ogcapi.model.TileMatrixSets;
import org.apache.baremaps.ogcapi.model.VectorTilesCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TilesResource implements TilesApi {

  private static final Logger logger = LoggerFactory.getLogger(TilesResource.class);

  @Inject
  public TilesResource() {

  }

  @Override
  public Response datasetVectorGetTile(String tileMatrix, Integer tileRow, Integer tileCol,
      String datetime, List<VectorTilesCollections> collections, List<String> subset, String crs,
      String subsetCrs, TileMatrixSets tileMatrixSetId, String f) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response datasetVectorGetTileSet(List<AllCollections> collections,
      TileMatrixSets tileMatrixSetId, String f) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Response datasetVectorGetTileSetsList(String f) {
    throw new UnsupportedOperationException();
  }
}
