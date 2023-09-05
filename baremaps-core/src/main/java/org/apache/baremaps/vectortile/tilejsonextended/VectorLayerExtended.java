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

package org.apache.baremaps.vectortile.tilejsonextended;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.apache.baremaps.vectortile.tilejson.VectorLayer;
import org.apache.baremaps.vectortile.tileset.TilesetQuery;

public class VectorLayerExtended extends VectorLayer {

  @JsonProperty("queries")
  List<TilesetQuery> queries = new ArrayList<>();

  public List<TilesetQuery> getQueries() {
    return queries;
  }
}
