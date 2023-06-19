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
import org.apache.baremaps.vectortile.style.Style;

/**
 * A resource that provides access to the style.
 */
@Singleton
@javax.ws.rs.Path("/")
public class StyleResource {

  private final Supplier<Style> styleSupplier;

  @Inject
  public StyleResource(Supplier<Style> styleSupplier) {
    this.styleSupplier = styleSupplier;
  }

  @GET
  @javax.ws.rs.Path("style.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Style getStyle() {
    return styleSupplier.get();
  }
}
