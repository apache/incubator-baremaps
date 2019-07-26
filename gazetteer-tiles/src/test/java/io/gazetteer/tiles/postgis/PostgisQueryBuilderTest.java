package io.gazetteer.tiles.postgis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.gazetteer.tiles.Tile;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PostgisQueryBuilderTest {

  private static final Tile Tile = new Tile(8567, 5773, 14);

  private static final List<PostgisLayer> LAYERS =
      Arrays.asList(
          new PostgisLayer(
              "buildings",
              "geom",
              0,
              20,
              "SELECT id, tags::jsonb, geom FROM osm_ways WHERE tags -> 'building' = 'yes' AND ST_Area(ST_Envelope(geom)) > {pixelArea}"),
          new PostgisLayer(
              "highways",
              "geom",
              0,
              20,
              "SELECT id, tags::jsonb, geom FROM osm_ways WHERE tags ? 'highway'"));

  private static final PostgisLayer LAYER = LAYERS.get(0);

  @Test
  public void buildLayer() {
    String sql = PostgisQueryBuilder.build(Tile, LAYER);
    assertEquals(
        "SELECT ST_AsMVT(buildings, 'buildings', 4096, 'geom') FROM (SELECT id, (tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', ''))))::jsonb, ST_AsMvtGeom(geom, ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857), 4096, 256, true) AS geom FROM (SELECT id, tags::jsonb, geom FROM osm_ways WHERE tags -> 'building' = 'yes' AND ST_Area(ST_Envelope(geom)) > 84.38049931026018) AS layer WHERE geom && ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857) AND ST_Intersects(geom, ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857))) as buildings",
        sql);
  }

  @Test
  public void buildLayers() {
    String sql = PostgisQueryBuilder.build(Tile, LAYERS);
    assertEquals(
        "SELECT ST_AsMVT(buildings, 'buildings', 4096, 'geom') || ST_AsMVT(highways, 'highways', 4096, 'geom') FROM (SELECT id, (tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', ''))))::jsonb, ST_AsMvtGeom(geom, ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857), 4096, 256, true) AS geom FROM (SELECT id, tags::jsonb, geom FROM osm_ways WHERE tags -> 'building' = 'yes' AND ST_Area(ST_Envelope(geom)) > 84.38049931026018) AS layer WHERE geom && ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857) AND ST_Intersects(geom, ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857))) as buildings, (SELECT id, (tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', ''))))::jsonb, ST_AsMvtGeom(geom, ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857), 4096, 256, true) AS geom FROM (SELECT id, tags::jsonb, geom FROM osm_ways WHERE tags ? 'highway') AS layer WHERE geom && ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857) AND ST_Intersects(geom, ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857))) as highways",
        sql);
  }

  @Test
  public void buildValues() {
    List<String> values = PostgisQueryBuilder.buildValues(LAYERS);
    assertNotNull(values);
    assertTrue(values.size() == 2);
  }

  @Test
  public void buildValue() {
    String sql = PostgisQueryBuilder.buildValue(LAYER);
    assertEquals("ST_AsMVT(buildings, 'buildings', 4096, 'geom')", sql);
  }

  @Test
  public void buildSources() {
    List<String> sources = PostgisQueryBuilder.buildSources(Tile, LAYERS);
    assertNotNull(sources);
    assertTrue(sources.size() == 2);
  }

  @Test
  public void buildSource() {
    String sql = PostgisQueryBuilder.buildSource(Tile, LAYER);
    assertEquals(
        "(SELECT id, (tags || hstore('geometry', lower(replace(st_geometrytype(geom), 'ST_', ''))))::jsonb, ST_AsMvtGeom(geom, ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857), 4096, 256, true) AS geom FROM (SELECT id, tags::jsonb, geom FROM osm_ways WHERE tags -> 'building' = 'yes' AND ST_Area(ST_Envelope(geom)) > 84.38049931026018) AS layer WHERE geom && ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857) AND ST_Intersects(geom, ST_MakeEnvelope(917244.339422115, 5914391.500593796, 919690.3243272407, 5916837.485498922, 3857))) as buildings",
        sql);
  }
}
