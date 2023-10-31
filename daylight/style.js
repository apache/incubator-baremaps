/**
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to you under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **/
import config from "./config.js";

import background from "../basemap/layers/background/style.js";
import aeroway_line from "../basemap/layers/aeroway/line.js";
import aeroway_polygon from "../basemap/layers/aeroway/polygon.js";
import amenity_background from "../basemap/layers/amenity/background.js";
import amenity_fountain from "../basemap/layers/amenity/fountain.js";
import amenity_overlay from "../basemap/layers/amenity/overlay.js";
import boundary_line from "../basemap/layers/boundary/line.js";
import landuse_background from "../basemap/layers/landuse/background.js";
import landuse_overlay from "../basemap/layers/landuse/overlay.js";
import natural_background from "../basemap/layers/natural/background.js";
import natural_overlay from "../basemap/layers/natural/overlay.js";
import natural_tree from "../basemap/layers/natural/tree.js";
import natural_trunk from "../basemap/layers/natural/trunk.js";
import power_background from "../basemap/layers/power/background.js";
import power_tower from "../basemap/layers/power/tower.js";
import power_cable from "../basemap/layers/power/cable.js";
import leisure_background from "../basemap/layers/leisure/background.js";
import leisure_overlay from "../basemap/layers/leisure/overlay.js";
import railway_tunnel from "../basemap/layers/railway/tunnel.js";
import railway_line from "../basemap/layers/railway/line.js";

import highway_line from '../basemap/layers/highway/highway_line.js';
import highway_outline from '../basemap/layers/highway/highway_outline.js';
import highway_dash from '../basemap/layers/highway/highway_dash.js';
import highway_tunnel_line from '../basemap/layers/highway/tunnel_line.js';
import highway_tunnel_outline from '../basemap/layers/highway/tunnel_outline.js';
import highway_pedestrian_area from '../basemap/layers/highway/pedestrian_area.js';
import highway_bridge_line from '../basemap/layers/highway/bridge_line.js';
import highway_bridge_outline from '../basemap/layers/highway/bridge_outline.js';
import highway_construction_line from "../basemap/layers/highway/construction_line.js";
import highway_construction_dash from "../basemap/layers/highway/construction_dash.js";
import highway_label from '../basemap/layers/highway/highway_label.js';

import coastline_overlay from './layers/coastline/overlay.js';
import route_line from "../basemap/layers/route/style.js"
import building_shape from "../basemap/layers/building/shape.js";
import building_number from "../basemap/layers/building/number.js";
import man_made_bridge from "../basemap/layers/man_made/bridge.js";
import man_made_pier_line from "../basemap/layers/man_made/pier_line.js";
import man_made_pier_label from "../basemap/layers/man_made/pier_label.js";
import waterway_line from "../basemap/layers/waterway/line.js"
import waterway_label from "../basemap/layers/waterway/label.js"
import waterway_tunnel_line from "../basemap/layers/waterway/tunnel_line.js"
import waterway_tunnel_casing from "../basemap/layers/waterway/tunnel_casing.js"
import icon from "../basemap/layers/point/icon.js";
import country_label from '../basemap/layers/point/country_label.js';
import point_label from '../basemap/layers/point/point_label.js';

export default {
    "version": 8,
    "name": "OpenStreetMapVecto",
    "center": config.center,
    "zoom": config.zoom,
    "sources": {
        "baremaps": {
            "type": "vector",
            "url": `${config.host}/tiles.json`
        }
    },
    "sprite": `https://baremaps.apache.org/sprites/osm/sprite`,
    "glyphs": "https://baremaps.apache.org/fonts/{fontstack}/{range}.pbf",
    "layers": [
        background,
        power_background,
        aeroway_polygon,
        landuse_background,
        leisure_background,
        amenity_background,
        natural_background,
        landuse_overlay,
        natural_overlay,
        amenity_overlay,
        leisure_overlay,
        coastline_overlay,
        waterway_line,
        waterway_tunnel_casing,
        waterway_tunnel_line,
        man_made_bridge,
        amenity_fountain,
        highway_tunnel_outline,
        highway_tunnel_line,
        railway_tunnel,
        building_shape,
        building_number,
        highway_construction_dash,
        highway_construction_line,
        highway_outline,
        highway_line,
        highway_dash,
        highway_pedestrian_area,
        railway_line,
        highway_bridge_outline,
        highway_bridge_line,
        highway_label,
        aeroway_line,
        route_line,
        power_cable,
        power_tower,
        man_made_pier_line,
        man_made_pier_label,
        natural_tree,
        natural_trunk,
        boundary_line,
        waterway_label,
        icon,
        point_label,
        country_label,
    ],
};