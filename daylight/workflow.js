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

export default {
    "steps": [
        {
            "id": "daylight-data",
            "needs": [],
            "tasks": [
                {
                    "type": "DownloadUrl",
                    "source": `https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/${config.daylightVersion}/planet-${config.daylightVersion}.osm.pbf`,
                    "target": "data/data.osm.pbf"
                },
                {
                    "type": "ImportOsmPbf",
                    "file": "data/data.osm.pbf",
                    "database": config.database,
                    "databaseSrid": 3857,
                    "replaceExisting": true,
                },
                {
                    "type": "DownloadUrl",
                    "source": `https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/${config.daylightVersion}/ml-buildings-${config.daylightVersion}.osm.pbf`,
                    "target": "data/buildings.osm.pbf"
                },
                {
                    "type": "ImportOsmPbf",
                    "file": "data/buildings.osm.pbf",
                    "database": config.database,
                    "databaseSrid": 3857,
                    "replaceExisting": false,
                },
                {
                    "type": "DownloadUrl",
                    "source": `https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/${config.daylightVersion}/fb-ml-roads-${config.daylightVersion}.osc.gz`,
                    "target": "data/roads.osc.gz"
                },
                {
                    "type": "ImportOsmOsc",
                    "file": "data/roads.osc.gz",
                    "compression": "gzip",
                    "database": config.database,
                    "databaseSrid": 3857
                },
                {
                    "type": "DownloadUrl",
                    "source": `https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/${config.daylightVersion}/admin-${config.daylightVersion}.osc.gz`,
                    "target": "data/admin.osc.gz"
                },
                {
                    "type": "ImportOsmOsc",
                    "file": "data/admin.osc.gz",
                    "compression": "gzip",
                    "database": config.database,
                    "databaseSrid": 3857
                },
                {
                    "type": "DownloadUrl",
                    "source": `https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/${config.daylightVersion}/coastlines-${config.daylightVersion}.tgz`,
                    "target": "data/coastlines.tgz"
                },
                {
                    "type": "DecompressFile",
                    "compression": "targz",
                    "source": "data/coastlines.tgz",
                    "target": "data/coastlines"
                },
                {
                    "type": "ImportShapefile",
                    "file": "data/coastlines/water_polygons.shp",
                    "database": config.database,
                    "fileSrid": 4326,
                    "databaseSrid": 3857
                },
                {
                    "type": "ExecuteSql",
                    "file": "./layers/coastline/prepare.sql",
                    "database": config.database,
                },
                {
                    "type": "DownloadUrl",
                    "source": `https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/${config.daylightVersion}/preferred-localization-${config.daylightVersion}.tsv`,
                    "target": "data/preferred-localization.tsv"
                },
                {
                    "type": "ImportDaylightTranslations",
                    "file": "data/preferred-localization.tsv",
                    "database": config.database,
                },
                {
                    "type": "DownloadUrl",
                    "source": `https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/${config.daylightVersion}/important-features-${config.daylightVersion}.json`,
                    "target": "data/important-features.json"
                },
                {
                    "type": "ImportDaylightFeatures",
                    "file": "data/important-features.json",
                    "database": config.database,
                },
                {
                    "type": "DownloadUrl",
                    "source": "https://daylight-openstreetmap.s3.us-west-2.amazonaws.com/landcover/low.shp",
                    "target": "data/landcover/low.shp"
                },
                {
                    "type": "DownloadUrl",
                    "source": "https://daylight-openstreetmap.s3.us-west-2.amazonaws.com/landcover/low.dbf",
                    "target": "data/landcover/low.dbf"
                },
                {
                    "type": "DownloadUrl",
                    "source": "https://daylight-openstreetmap.s3.us-west-2.amazonaws.com/landcover/low.prj",
                    "target": "data/landcover/low.prj"
                },
                {
                    "type": "DownloadUrl",
                    "source": "https://daylight-openstreetmap.s3.us-west-2.amazonaws.com/landcover/low.shx",
                    "target": "data/landcover/low.shx"
                },
                {
                    "type": "ImportShapefile",
                    "file": "data/landcover/low.shp",
                    "database": config.database,
                    "fileSrid": 4326,
                    "databaseSrid": 3857
                },
            ]
        },
        {
            "id": "daylight-nodes",
            "needs": ["daylight-data"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/queries/osm_node.sql",
                    "database": config.database,
                    "parallel": true,
                },
            ]
        },
        {
            "id": "daylight-ways",
            "needs": ["daylight-data"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/queries/osm_way.sql",
                    "database": config.database,
                    "parallel": true,
                },
            ]
        },
        {
            "id": "daylight-relations",
            "needs": ["daylight-data"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/queries/osm_relation.sql",
                    "database": config.database,
                    "parallel": true,
                },
            ]
        },
        {
            "id": "daylight-member",
            "needs": ["daylight-data"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/member/prepare.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "daylight-point",
            "needs": ["daylight-nodes"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/point/clean.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/point/simplify.sql",
                    "database": config.database,
                    "parallel": true,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/point/index.sql",
                    "database": config.database,
                    "parallel": true,
                },
            ]
        },
        {
            "id": "daylight-linestring",
            "needs": ["daylight-member"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/linestring/clean.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/linestring/prepare.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/linestring/index.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "daylight-highway",
            "needs": ["daylight-linestring"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/highway/clean.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/highway/prepare.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/highway/simplify.sql",
                    "database": config.database,
                    "parallel": true,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/highway/index.sql",
                    "database": config.database,
                    "parallel": true,
                },
            ]
        },
        {
            "id": "daylight-railway",
            "needs": ["daylight-linestring"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/railway/clean.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/railway/prepare.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/railway/simplify.sql",
                    "database": config.database,
                    "parallel": true,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/railway/index.sql",
                    "database": config.database,
                    "parallel": true,
                },
            ]
        },
        {
            "id": "daylight-route",
            "needs": ["daylight-linestring"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/route/clean.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/route/prepare.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/route/simplify.sql",
                    "database": config.database,
                    "parallel": true,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/route/index.sql",
                    "database": config.database,
                    "parallel": true,
                },
            ]
        },
        {
            "id": "daylight-waterway",
            "needs": ["daylight-linestring"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/waterway/clean.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/waterway/prepare.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/waterway/simplify.sql",
                    "database": config.database,
                    "parallel": true,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/waterway/index.sql",
                    "database": config.database,
                    "parallel": true
                },
            ]
        },
        {
            "id": "daylight-polygon",
            "needs": ["daylight-member"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/polygon/clean.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/polygon/prepare.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/polygon/index.sql",
                    "database": config.database,
                },
            ]
        },
        {
            "id": "daylight-natural",
            "needs": ["daylight-polygon"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/natural/clean.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/natural/prepare.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/natural/simplify.sql",
                    "database": config.database,
                    "parallel": true,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/natural/index.sql",
                    "database": config.database,
                    "parallel": true
                },
            ]
        },
        {
            "id": "daylight-landuse",
            "needs": ["daylight-polygon"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/landuse/clean.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/landuse/prepare.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/landuse/simplify.sql",
                    "database": config.database,
                    "parallel": true,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/landuse/index.sql",
                    "database": config.database,
                    "parallel": true
                },
            ]
        },
        {
            "id": "daylight-leisure",
            "needs": ["daylight-polygon"],
            "tasks": [
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/leisure/clean.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/leisure/prepare.sql",
                    "database": config.database,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/leisure/simplify.sql",
                    "database": config.database,
                    "parallel": true,
                },
                {
                    "type": "ExecuteSql",
                    "file": "../basemap/layers/leisure/index.sql",
                    "database": config.database,
                    "parallel": true
                },
            ]
        },
    ]
}
