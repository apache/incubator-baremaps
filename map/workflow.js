export default {
  "steps": [
    {
      "id": "natural-earth",
      "needs": [],
      "tasks": [
        {
          "type": "DownloadUrl",
          "url": "https://naciscdn.org/naturalearth/packages/natural_earth_vector.gpkg.zip",
          "path": "data/natural_earth_vector.gpkg.zip"
        },
        {
          "type": "UnzipFile",
          "file": "data/natural_earth_vector.gpkg.zip",
          "directory": "data/natural_earth_vector"
        },
        {
          "type": "ImportGeoPackage",
          "file": "data/natural_earth_vector/packages/natural_earth_vector.gpkg",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
          "sourceSRID": 4326,
          "targetSRID": 3857
        },
        {
          "type": "ExecuteSql",
          "file": "queries/ne_index.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        }
      ]
    },
    {
      "id": "water-polygons",
      "needs": [],
      "tasks": [
        {
          "type": "DownloadUrl",
          "url": "https://osmdata.openstreetmap.de/download/water-polygons-split-3857.zip",
          "path": "data/water-polygons-split-3857.zip"
        },
        {
          "type": "UnzipFile",
          "file": "data/water-polygons-split-3857.zip",
          "directory": "data"
        },
        {
          "type": "ImportShapefile",
          "file": "data/water-polygons-split-3857/water_polygons.shp",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
          "sourceSRID": 3857,
          "targetSRID": 3857
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_water_index.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        }
      ]
    },
    {
      "id": "simplified-water-polygons",
      "needs": [],
      "tasks": [
        {
          "type": "DownloadUrl",
          "url": "https://osmdata.openstreetmap.de/download/simplified-water-polygons-split-3857.zip",
          "path": "data/simplified-water-polygons-split-3857.zip"
        },
        {
          "type": "UnzipFile",
          "file": "data/simplified-water-polygons-split-3857.zip",
          "directory": "data"
        },
        {
          "type": "ImportShapefile",
          "file": "data/simplified-water-polygons-split-3857/simplified_water_polygons.shp",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
          "sourceSRID": 3857,
          "targetSRID": 3857
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_simplified_water_index.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
      ]
    },
    {
      "id": "openstreetmap",
      "needs": [],
      "tasks": [
        {
          "type": "DownloadUrl",
          "url": "https://download.geofabrik.de/europe/switzerland-latest.osm.pbf",
          "path": "data/data.osm.pbf"
        },
        {
          "type": "ImportOpenStreetMap",
          "file": "data/data.osm.pbf",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
          "databaseSrid": 3857
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_nodes.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_ways.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_ways_member.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_relations.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_linestring.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_polygon.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_boundary.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_highway.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_railway.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_natural.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_landuse.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
        },
      ]
    }
  ]
}
