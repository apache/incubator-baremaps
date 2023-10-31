/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
import config from "./config.js";

import aerialway from "../basemap/layers/aerialway/tileset.js";
import aeroway from "../basemap/layers/aeroway/tileset.js";
import amenity from "../basemap/layers/amenity/tileset.js";
import attraction from "../basemap/layers/attraction/tileset.js";
import barrier from "../basemap/layers/barrier/tileset.js";
import boundary from "../basemap/layers/boundary/tileset.js";
import building from "../basemap/layers/building/tileset.js";
import highway from "../basemap/layers/highway/tileset.js";
import natural from "../basemap/layers/natural/tileset.js";
import leisure from "../basemap/layers/leisure/tileset.js";
import landuse from "../basemap/layers/landuse/tileset.js";
import railway from "../basemap/layers/railway/tileset.js";
import route from "../basemap/layers/route/tileset.js";
import man_made from "../basemap/layers/man_made/tileset.js";
import power from "../basemap/layers/power/tileset.js";
import point from "../basemap/layers/point/tileset.js";
import waterway from "../basemap/layers/waterway/tileset.js";
import coastline from "./layers/coastline/tileset.js";


export default {
  "tilejson": "2.2.0",
  "center": [...config.center, config.zoom],
  "bounds": [...config.bounds],
  "minzoom": 0.0,
  "maxzoom": 14.0,
  "tiles": [
    `${config.host}/tiles/{z}/{x}/{y}.mvt`
  ],
  attribution: '© <a href="https://www.openstreetmap.org/">OpenStreetMap</a> © <a href="https://www.geoboundaries.org">geoBoundaries</a>',
  database: config.database,
  "vector_layers": [
    aerialway,
    aeroway,
    amenity,
    attraction,
    barrier,
    boundary,
    building,
    highway,
    landuse,
    leisure,
    man_made,
    natural,
    coastline,
    point,
    power,
    railway,
    route,
    waterway,
  ]
}
