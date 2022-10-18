import { database } from './config.js';

export default {
  "steps": [
    {
      "id": "natural-earth",
      "needs": [],
      "tasks": [
        {
          "type": "org.apache.baremaps.workflow.tasks.DownloadUrl",
          "url": "https://naciscdn.org/naturalearth/packages/natural_earth_vector.gpkg.zip",
          "path": "data/natural_earth_vector.gpkg.zip"
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.UnzipFile",
          "file": "data/natural_earth_vector.gpkg.zip",
          "directory": "data/natural_earth_vector"
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ImportGeoPackage",
          "file": "data/natural_earth_vector/packages/natural_earth_vector.gpkg",
          "database": database,
          "sourceSRID": 4326,
          "targetSRID": 3857
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/ne_index.sql",
          "database": database
        }
      ]
    },
    {
      "id": "water-polygons",
      "needs": [],
      "tasks": [
        {
          "type": "org.apache.baremaps.workflow.tasks.DownloadUrl",
          "url": "https://osmdata.openstreetmap.de/download/water-polygons-split-3857.zip",
          "path": "data/water-polygons-split-3857.zip"
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.UnzipFile",
          "file": "data/water-polygons-split-3857.zip",
          "directory": "data"
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ImportShapefile",
          "file": "data/water-polygons-split-3857/water_polygons.shp",
          "database": database,
          "sourceSRID": 3857,
          "targetSRID": 3857
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_water_index.sql",
          "database": database
        }
      ]
    },
    {
      "id": "simplified-water-polygons",
      "needs": [],
      "tasks": [
        {
          "type": "org.apache.baremaps.workflow.tasks.DownloadUrl",
          "url": "https://osmdata.openstreetmap.de/download/simplified-water-polygons-split-3857.zip",
          "path": "data/simplified-water-polygons-split-3857.zip"
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.UnzipFile",
          "file": "data/simplified-water-polygons-split-3857.zip",
          "directory": "data"
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ImportShapefile",
          "file": "data/simplified-water-polygons-split-3857/simplified_water_polygons.shp",
          "database": database,
          "sourceSRID": 3857,
          "targetSRID": 3857
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_simplified_water_index.sql",
          "database": database
        },
      ]
    },
    {
      "id": "openstreetmap",
      "needs": [],
      "tasks": [
        {
          "type": "org.apache.baremaps.workflow.tasks.DownloadUrl",
          "url": "https://download.geofabrik.de/europe/switzerland-latest.osm.pbf",
          "path": "data/data.osm.pbf"
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ImportOpenStreetMap",
          "file": "data/data.osm.pbf",
          "database": database,
          "databaseSrid": 3857
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_node.sql",
          "database": database
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_way.sql",
          "database": database
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_way_member.sql",
          "database": database
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_linestring.sql",
          "database": database
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_polygon.sql",
          "database": database
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_boundary.sql",
          "database": database
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_highway.sql",
          "database": database
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_railway.sql",
          "database": database
        },
        {
          "type": "org.apache.baremaps.workflow.tasks.ExecuteSql",
          "file": "queries/osm_relation.sql",
          "database": database
        },
      ]
    }
  ]
}
