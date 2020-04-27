# Building Example

This example builds upon the OpenStreetMap example and shows how 3d buildings can be displayed with Mapbox.
First, start by downloading the OSM data for London in the current directory.

```
wget https://download.geofabrik.de/europe/great-britain/england/greater-london-latest.osm.pbf
```

You can then import the data in Postgresql using the following command:

```bash
baremaps import \
  --input 'greater-london-latest.osm.pbf' \
  --database 'jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps'
```

In the configuration file, notice the SQL query associated with the building layer.
Here, the number of levels stored in OSM is multiplied by 3, which rawly correspond to the height of a level in meters.

```sql
SELECT 
    id, 
    tags || hstore('building:height'::text, 
        ((CASE WHEN tags -> 'building:levels' ~ '^[0-9\.]+$' THEN tags -> 'building:levels' ELSE '1' END)::real * 3)::text), 
    geom 
FROM osm_ways WHERE tags ? 'building'
```

This property is then used in the style to extrude the buildings.
To preview this example, start the tile server:

```bash
baremaps serve \
  --database 'jdbc:postgresql://localhost:5432/baremaps?allowMultiQueries=true&user=baremaps&password=baremaps' \
  --config 'config.yaml' \
  --assets 'static/'
```

Well done, a map of London should appear in your browser ([http://localhost:9000/](http://localhost:9000/))!
