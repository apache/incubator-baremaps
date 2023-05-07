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


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.ogcapi.api.StylesApi;
import org.apache.baremaps.ogcapi.model.StyleSet;
import org.apache.baremaps.ogcapi.model.StyleSetEntry;
import org.apache.baremaps.vectortile.style.Style;
import org.apache.baremaps.vectortile.style.StyleSource;

/**
 * The styles resource.
 */
@Singleton
public class StylesResource implements StylesApi {

  private final Style style;

  /**
   * Constructs a {@code StylesResource}.
   *
   * @param style
   * @param objectMapper
   * @throws IOException
   */
  @Inject
  public StylesResource(@Context UriInfo uriInfo, @Named("style") Path style,
      ObjectMapper objectMapper) throws IOException {
    this.style = objectMapper.readValue(new ConfigReader().read(style), Style.class);
    var source = new StyleSource();
    source.setType("vector");
    source.setUrl(uriInfo.getBaseUri().toString() + "tiles/default");
    this.style.setSources(Map.of("baremaps", source));
  }

  /**
   * Get the style set.
   */
  @Override
  public Response getStyleSet() {
    var styleSetEntry = new StyleSetEntry();
    styleSetEntry.setId("default");
    var styleSet = new StyleSet();
    styleSet.setStyles(List.of(styleSetEntry));
    return Response.ok(styleSet).build();
  }

  /**
   * Get the style.
   */
  @Override
  public Response getStyle(String styleId) {
    return Response.ok(style).build();
  }

  /**
   * Get the style metadata.
   */
  @Override
  public Response getStyleMetadata(String styleId) {
    throw new UnsupportedOperationException();
  }
}
