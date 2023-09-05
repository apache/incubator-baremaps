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

import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.baremaps.vectortile.tilejsonextended.TileJSONExtended;

/**
 * A resource that provides access to the tileJSON extended version
 */
@Singleton
@javax.ws.rs.Path("/")
public class TileJsonExtendedResource {

  private final Supplier<TileJSONExtended> tileJSONSupplier;

  @Inject
  public TileJsonExtendedResource(Supplier<TileJSONExtended> tileJSONSupplier) {
    this.tileJSONSupplier = tileJSONSupplier;
  }

  @GET
  @javax.ws.rs.Path("tiles-extended.json")
  @Produces(MediaType.APPLICATION_JSON)
  public TileJSONExtended getTileset() {
    return tileJSONSupplier.get();
  }

}
