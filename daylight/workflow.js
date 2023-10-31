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
          "url": "https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/v1.33/planet-v1.33.osm.pbf",
          "path": "data/data.osm.pbf"
        },
        {
          "type": "ImportOsmPbf",
          "file": "data/data.osm.pbf",
          "database": config.database,
          "databaseSrid": 3857,
          "replaceExisting": true,
        },
      ]
    },
    {
      "id": "daylight-buildings",
      "needs": ["daylight-data"],
      "tasks": [
        {
          "type": "DownloadUrl",
          "url": "https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/v1.33/ml-buildings-v1.33.osm.pbf",
          "path": "data/buildings.osm.pbf"
        },
        {
          "type": "ImportOsmPbf",
          "file": "data/buildings.osm.pbf",
          "database": config.database,
          "databaseSrid": 3857,
          "replaceExisting": false,
        },
      ]
    },
    {
      "id": "daylight-roads",
      "needs": ["daylight-buildings"],
      "tasks": [
        {
          "type": "DownloadUrl",
          "url": "https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/v1.33/fb-ml-roads-v1.33.osc.gz",
          "path": "data/roads.osc.gz"
        },
        {
          "type": "ImportOsmChange",
          "file": "data/roads.osc.gz",
          "compression": "gzip",
          "database": config.database,
          "srid": 3857
        },
      ]
    },
    {
      "id": "daylight-admin",
      "needs": ["daylight-roads"],
      "tasks": [
        {
          "type": "DownloadUrl",
          "url": "https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/v1.33/admin-v1.33.osc.gz",
          "path": "data/admin.osc.gz"
        },
        {
          "type": "ImportOsmChange",
          "file": "data/admin.osc.gz",
          "compression": "gzip",
          "database": config.database,
          "srid": 3857
        },
      ]
    },
    {
      "id": "daylight-coastlines",
      "needs": ["daylight-data"],
      "tasks": [
        {
          "type": "DownloadUrl",
          "url": "https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/v1.33/coastlines-v1.33.tgz",
          "path": "data/coastlines.tgz"
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
          "sourceSRID": 4326,
          "targetSRID": 3857
        },
        {
          "type": "ExecuteSql",
          "file": "./layers/coastline/clean.sql",
          "database": config.database,
        },
        {
          "type": "ExecuteSql",
          "file": "./layers/coastline/prepare.sql",
          "database": config.database,
        },
        {
          "type": "ExecuteSql",
          "file": "./layers/coastline/index.sql",
          "database": config.database,
        },
      ]
    },
    {
      "id": "daylight-preferred-localization",
      "needs": ["daylight-data"],
      "tasks": [
        {
          "type": "DownloadUrl",
          "url": "https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/v1.33/preferred-localization-v1.33.tsv",
          "path": "data/preferred-localization.tsv"
        },
      ]
    },
    {
      "id": "daylight-important-features",
      "needs": ["daylight-data"],
      "tasks": [
        {
          "type": "DownloadUrl",
          "url": "https://daylight-map-distribution.s3.us-west-1.amazonaws.com/release/v1.32/important-features-v1.32.json",
          "path": "data/important-features.json"
        },
      ]
    },
    {
      "id": "daylight-landcover",
        "needs": ["daylight-data"],
        "tasks": [
          {
            "type": "DownloadUrl",
            "url": "https://daylight-openstreetmap.s3.us-west-2.amazonaws.com/landcover/low.shp",
            "path": "data/landcover/low.shp"
          },
          {
            "type": "DownloadUrl",
            "url": "https://daylight-openstreetmap.s3.us-west-2.amazonaws.com/landcover/low.dbf",
            "path": "data/landcover/low.dbf"
          },
          {
            "type": "DownloadUrl",
            "url": "https://daylight-openstreetmap.s3.us-west-2.amazonaws.com/landcover/low.prj",
            "path": "data/landcover/low.prj"
          },
          {
            "type": "DownloadUrl",
            "url": "https://daylight-openstreetmap.s3.us-west-2.amazonaws.com/landcover/low.shx",
            "path": "data/landcover/low.shx"
          },
          {
            "type": "ImportShapefile",
            "file": "data/landcover/low.shp",
            "database": config.database,
            "sourceSRID": 4326,
            "targetSRID": 3857
          },
        ]
    },
    {
      "id": "daylight-nodes",
      "needs": ["daylight-admin", "daylight-coastlines", "daylight-preferred-localization", "daylight-important-features"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "../basemap/queries/osm_nodes.sql",
          "database": config.database,
          "parallel": true,
        },
      ]
    },
    {
      "id": "daylight-ways",
      "needs": ["daylight-admin", "daylight-coastlines", "daylight-preferred-localization", "daylight-important-features"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "../basemap/queries/osm_ways.sql",
          "database": config.database,
          "parallel": true,
        },
      ]
    },
    {
      "id": "daylight-relations",
      "needs": ["daylight-admin", "daylight-coastlines", "daylight-preferred-localization", "daylight-important-features"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "../basemap/queries/osm_relations.sql",
          "database": config.database,
          "parallel": true,
        },
      ]
    },
    {
      "id": "daylight-member",
      "needs": ["daylight-admin", "daylight-coastlines", "daylight-preferred-localization", "daylight-important-features"],
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
  ]
}
