export default {
  "steps": [
    // {
    //   "id": "natural-earth",
    //   "needs": [],
    //   "tasks": [
    //     {
    //       "type": "DownloadUrl",
    //       "url": "https://naciscdn.org/naturalearth/packages/natural_earth_vector.gpkg.zip",
    //       "path": "data/natural_earth_vector.gpkg.zip"
    //     },
    //     {
    //       "type": "UnzipFile",
    //       "file": "data/natural_earth_vector.gpkg.zip",
    //       "directory": "data/natural_earth_vector"
    //     },
    //     {
    //       "type": "ImportGeoPackage",
    //       "file": "data/natural_earth_vector/packages/natural_earth_vector.gpkg",
    //       "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
    //       "sourceSRID": 4326,
    //       "targetSRID": 3857
    //     },
    //     {
    //       "type": "ExecuteSql",
    //       "file": "queries/ne_index.sql",
    //       "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
    //       "parallel": true,
    //     }
    //   ]
    // },
    // {
    //   "id": "water-polygons",
    //   "needs": [],
    //   "tasks": [
    //     {
    //       "type": "DownloadUrl",
    //       "url": "https://osmdata.openstreetmap.de/download/water-polygons-split-3857.zip",
    //       "path": "data/water-polygons-split-3857.zip"
    //     },
    //     {
    //       "type": "UnzipFile",
    //       "file": "data/water-polygons-split-3857.zip",
    //       "directory": "data"
    //     },
    //     {
    //       "type": "ImportShapefile",
    //       "file": "data/water-polygons-split-3857/water_polygons.shp",
    //       "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
    //       "sourceSRID": 3857,
    //       "targetSRID": 3857
    //     },
    //     {
    //       "type": "ExecuteSql",
    //       "file": "queries/osm_water_index.sql",
    //       "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps"
    //     }
    //   ]
    // },
    // {
    //   "id": "simplified-water-polygons",
    //   "needs": [],
    //   "tasks": [
    //     {
    //       "type": "DownloadUrl",
    //       "url": "https://osmdata.openstreetmap.de/download/simplified-water-polygons-split-3857.zip",
    //       "path": "data/simplified-water-polygons-split-3857.zip"
    //     },
    //     {
    //       "type": "UnzipFile",
    //       "file": "data/simplified-water-polygons-split-3857.zip",
    //       "directory": "data"
    //     },
    //     {
    //       "type": "ImportShapefile",
    //       "file": "data/simplified-water-polygons-split-3857/simplified_water_polygons.shp",
    //       "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
    //       "sourceSRID": 3857,
    //       "targetSRID": 3857
    //     },
    //     {
    //       "type": "ExecuteSql",
    //       "file": "queries/osm_simplified_water_index.sql",
    //       "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
    //     },
    //   ]
    // },
    {
      "id": "openstreetmap-data",
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
      ]
    },
    {
      "id": "openstreetmap-nodes",
      "needs": ["openstreetmap-data"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "queries/osm_nodes_clean.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_nodes_prepare.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_nodes_simplify.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_nodes_index.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
      ]
    },
    {
      "id": "openstreetmap-ways",
      "needs": ["openstreetmap-data"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "queries/osm_ways_clean.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_ways_prepare.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_ways_simplify.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_ways_index.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
      ]
    },
    {
      "id": "openstreetmap-relations",
      "needs": ["openstreetmap-data"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "queries/osm_relations_clean.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_relations_prepare.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_relations_simplify.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_relations_index.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
      ]
    },
    {
      "id": "openstreetmap-linestring",
      "needs": ["openstreetmap-ways"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "queries/osm_linestring.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
      ]
    },
    {
      "id": "openstreetmap-polygon",
      "needs": ["openstreetmap-ways", "openstreetmap-relations"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "queries/osm_polygon_prepare.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_polygon_index.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
      ]
    },
    {
      "id": "openstreetmap-boundary",
      "needs": ["openstreetmap-linestring"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "queries/osm_boundary_prepare.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_boundary_simplify.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_boundary_index.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
      ]
    },
    {
      "id": "openstreetmap-highway",
      "needs": ["openstreetmap-linestring"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "queries/osm_highway_prepare.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_highway_simplify.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_highway_index.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
      ]
    },
    {
      "id": "openstreetmap-railway",
      "needs": ["openstreetmap-linestring"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "queries/osm_railway_prepare.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_railway_simplify.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_railway_index.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
      ]
    },
    {
      "id": "openstreetmap-natural",
      "needs": ["openstreetmap-polygon"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "queries/osm_natural_prepare.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_natural_simplify.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
      ]
    },
    {
      "id": "openstreetmap-landuse",
      "needs": ["openstreetmap-polygon"],
      "tasks": [
        {
          "type": "ExecuteSql",
          "file": "queries/osm_landuse_prepare.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
        {
          "type": "ExecuteSql",
          "file": "queries/osm_landuse_simplify.sql",
          "database": "jdbc:postgresql://localhost:5432/baremaps?&user=baremaps&password=baremaps",
        },
      ]
    }
  ]
}
