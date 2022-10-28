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

package org.apache.baremaps.tileset;



import java.util.ArrayList;
import java.util.List;

/**
 * A loose implementation of the Mapbox TileJSON specification.
 *
 * @see <a href=
 *      "https://docs.mapbox.com/help/glossary/tilejson/">https://docs.mapbox.com/help/glossary/tilejson/</a>
 */
public class Tileset {

  private String tilejson = "2.2.0";
  private String name;
  private String description;
  private String version;
  private String attribution;
  private String template;
  private String legend;
  private String scheme;
  private List<String> tiles = new ArrayList<>();
  private List<String> grids = new ArrayList<>();
  private List<String> data = new ArrayList<>();
  private Integer minzoom;
  private Integer maxzoom;
  private List<Double> bounds = new ArrayList<>();
  private List<Double> center = new ArrayList<>();
  private List<TilesetLayer> vectorLayers = new ArrayList<>();
  private Integer fillzoom;

  public Tileset() {}

  public Tileset(String tilejson, String name, String description, String version,
      String attribution, String template, String legend, String scheme, List<String> tiles,
      List<String> grids, List<String> data, Integer minzoom, Integer maxzoom, List<Double> bounds,
      List<Double> center, List<TilesetLayer> vectorLayers, Integer fillzoom) {
    this.tilejson = tilejson;
    this.name = name;
    this.description = description;
    this.version = version;
    this.attribution = attribution;
    this.template = template;
    this.legend = legend;
    this.scheme = scheme;
    this.tiles = tiles;
    this.grids = grids;
    this.data = data;
    this.minzoom = minzoom;
    this.maxzoom = maxzoom;
    this.bounds = bounds;
    this.center = center;
    this.vectorLayers = vectorLayers;
    this.fillzoom = fillzoom;
  }

  public String getTilejson() {
    return tilejson;
  }

  public Tileset setTilejson(String tilejson) {
    this.tilejson = tilejson;
    return this;
  }

  public String getName() {
    return name;
  }

  public Tileset setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Tileset setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public Tileset setVersion(String version) {
    this.version = version;
    return this;
  }

  public String getAttribution() {
    return attribution;
  }

  public Tileset setAttribution(String attribution) {
    this.attribution = attribution;
    return this;
  }

  public String getTemplate() {
    return template;
  }

  public Tileset setTemplate(String template) {
    this.template = template;
    return this;
  }

  public String getLegend() {
    return legend;
  }

  public Tileset setLegend(String legend) {
    this.legend = legend;
    return this;
  }

  public String getScheme() {
    return scheme;
  }

  public Tileset setScheme(String scheme) {
    this.scheme = scheme;
    return this;
  }

  public List<String> getTiles() {
    return tiles;
  }

  public Tileset setTiles(List<String> tiles) {
    this.tiles = tiles;
    return this;
  }

  public List<String> getGrids() {
    return grids;
  }

  public Tileset setGrids(List<String> grids) {
    this.grids = grids;
    return this;
  }

  public List<String> getData() {
    return data;
  }

  public Tileset setData(List<String> data) {
    this.data = data;
    return this;
  }

  public Integer getMinzoom() {
    return minzoom;
  }

  public Tileset setMinzoom(Integer minzoom) {
    this.minzoom = minzoom;
    return this;
  }

  public Integer getMaxzoom() {
    return maxzoom;
  }

  public Tileset setMaxzoom(Integer maxzoom) {
    this.maxzoom = maxzoom;
    return this;
  }

  public List<Double> getBounds() {
    return bounds;
  }

  public Tileset setBounds(List<Double> bounds) {
    this.bounds = bounds;
    return this;
  }

  public List<Double> getCenter() {
    return center;
  }

  public Tileset setCenter(List<Double> center) {
    this.center = center;
    return this;
  }

  public List<TilesetLayer> getVectorLayers() {
    return vectorLayers;
  }

  public Tileset setVectorLayers(List<TilesetLayer> vectorLayers) {
    this.vectorLayers = vectorLayers;
    return this;
  }

  public Integer getFillzoom() {
    return fillzoom;
  }

  public Tileset setFillzoom(Integer fillzoom) {
    this.fillzoom = fillzoom;
    return this;
  }
}
