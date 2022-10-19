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

package org.apache.baremaps.http.ogcapi;



import java.util.List;
import javax.inject.Singleton;
import org.apache.baremaps.database.tile.PostgresQuery;
import org.apache.baremaps.model.TileJSON;

@Singleton
public class Conversions {

  public static List<PostgresQuery> asPostgresQuery(TileJSON tileJSON) {
    return tileJSON.getVectorLayers().stream()
        .flatMap(layer -> layer.getQueries().stream().map(query -> new PostgresQuery(layer.getId(),
            query.getMinzoom(), query.getMaxzoom(), query.getSql())))
        .toList();
  }
}
