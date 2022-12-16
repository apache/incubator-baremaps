import config from "./config.js";

import layer from './utils/layer.js';

import background from "./layers/background/style.js";
import amenity_background from "./layers/amenity/background.js";
import amenity_fountain from "./layers/amenity/fountain.js";
import boundary_line from "./layers/boundary/line.js"
import landuse_background from "./layers/landuse/background.js";
import landuse_overlay from "./layers/landuse/overlay.js";
import natural_background from "./layers/natural/background.js";
import natural_overlay from "./layers/natural/overlay.js";
import natural_tree from "./layers/natural/tree.js";
import natural_trunk from "./layers/natural/trunk.js";
import power_background from "./layers/power/background.js";
import power_tower from "./layers/power/tower.js";
import power_cable from "./layers/power/cable.js";
import leisure_background from "./layers/leisure/background.js";
import leisure_overlay from "./layers/leisure/overlay.js";
import railway_tunnel from "./layers/railway/tunnel.js";
import railway_line from "./layers/railway/line.js";
import highway_line from './layers/highway/highway_line.js';
import highway_outline from './layers/highway/highway_outline.js';
import highway_dash from './layers/highway/highway_dash.js';
import highway_tunnel_line from './layers/highway/tunnel_line.js';
import highway_tunnel_outline from './layers/highway/tunnel_outline.js';
import highway_pedestrian_area from './layers/highway/pedestrian_area.js';
import highway_bridge_line from './layers/highway/bridge_line.js';
import highway_bridge_outline from './layers/highway/bridge_outline.js';
import highway_label from './layers/highway/highway_label.js';
import ocean_background from './layers/ocean/background.js';
import route_line from "./layers/route/style.js"
import building_shape from "./layers/building/shape.js";
import building_number from "./layers/building/number.js";
import man_made_pier_line from "./layers/man_made/pier_line.js";
import man_made_pier_label from "./layers/man_made/pier_label.js";
import waterway_line from "./layers/waterway/line.js"
import waterway_label from "./layers/waterway/label.js"
import waterway_tunnel_line from "./layers/waterway/tunnel_line.js"
import waterway_tunnel_casing from "./layers/waterway/tunnel_casing.js"
import icon from "./layers/point/icon.js";
import label from './layers/point/label.js';

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
    "sprite": `https://tiles.baremaps.com/sprites/osm/sprite`,
    "glyphs": "https://tiles.baremaps.com/fonts/{fontstack}/{range}.pbf",
    "layers": [
        background,
        layer(power_background),
        layer(amenity_background),
        layer(landuse_background),
        layer(leisure_background),
        layer(natural_background),
        layer(ocean_background),
        layer(landuse_overlay),
        layer(leisure_overlay),
        layer(natural_overlay),
        layer(waterway_line),
        layer(waterway_tunnel_casing),
        layer(waterway_tunnel_line),
        layer(amenity_fountain),
        layer(highway_tunnel_outline),
        layer(highway_tunnel_line),
        layer(railway_tunnel),
        layer(highway_outline),
        layer(highway_line),
        layer(highway_dash),
        layer(highway_pedestrian_area),
        layer(railway_line),
        layer(highway_bridge_outline),
        layer(highway_bridge_line),
        layer(highway_label),
        layer(route_line),
        layer(power_cable),
        layer(power_tower),
        layer(building_shape),
        layer(building_number),
        layer(man_made_pier_line),
        layer(man_made_pier_label),
        layer(natural_tree),
        layer(natural_trunk),
        layer(boundary_line),
        layer(waterway_label),
        layer(icon),
        layer(label),
    ],
};
