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

package org.apache.baremaps.server.tilejson;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public record TileJSON(

    @NotNull String tilejson,
    @JsonProperty("tiles")
    @Valid
    @NotNull List<String> tiles,
    @JsonProperty("vector_layers")
    @Valid
    @NotNull List<VectorLayer> vectorLayers,
    @JsonProperty("attribution") String attribution,
    @JsonProperty("bounds")
    @Valid List<Double> bounds,
    @JsonProperty("center")
    @Valid List<Double> center,
    @JsonProperty("data")
    @Valid List<String> data,
    @JsonProperty("description") String description,
    @JsonProperty("fillzoom")
    @DecimalMin("0")
    @DecimalMax("30") Integer fillzoom,
    @JsonProperty("grids")
    @Valid List<String> grids,
    @JsonProperty("legend") String legend,
    @JsonProperty("maxzoom")
    @DecimalMin("0")
    @DecimalMax("30") Integer maxzoom,
    @JsonProperty("minzoom")
    @DecimalMin("0")
    @DecimalMax("30") Integer minzoom,
    @JsonProperty("name") String name,
    @JsonProperty("scheme") String scheme,
    @JsonProperty("template") String template,
    @JsonProperty("version")
    @Pattern(regexp = "\\d+\\.\\d+\\.\\d+\\w?[\\w\\d]*") String version) {
}
