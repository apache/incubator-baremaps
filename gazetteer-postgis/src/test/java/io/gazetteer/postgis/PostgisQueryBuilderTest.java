package io.gazetteer.postgis;

import io.gazetteer.core.XYZ;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PostgisQueryBuilderTest {

  private static final XYZ XYZ = new XYZ(8567, 5773, 14);

  private static final List<PostgisLayer> LAYERS =
      Arrays.asList(
          new PostgisLayer(
              "buildings",
              "geom",
              0,
              20,
              "SELECT id, geom FROM ways WHERE tags -> 'building' = 'yes'"),
          new PostgisLayer(
              "highways",
              "geom",
              0,
              20,
              " SELECT id, geom FROM ways WHERE tags -> 'highway' = 'path'"));

  private static final PostgisLayer LAYER = LAYERS.get(0);

  @Test
  public void buildLayer() {
    String sql = PostgisQueryBuilder.build(XYZ, LAYER);
    assertEquals(
        "SELECT ST_AsMVT(buildings, 'buildings', 4096, 'geometry') "
            + "FROM (SELECT id, properties, ST_AsMvtGeom(geometry, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845), 4096, 256, true) AS geometry "
            + "FROM (SELECT id, geom FROM ways WHERE tags -> 'building' = 'yes') AS layer "
            + "WHERE geometry && ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845) "
            + "AND ST_Intersects(geometry, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845))) as buildings",
        sql);
  }

  @Test
  public void buildLayers() {
    String sql = PostgisQueryBuilder.build(XYZ, LAYERS);
    assertEquals(
        "SELECT ST_AsMVT(buildings, 'buildings', 4096, 'geometry') || ST_AsMVT(highways, 'highways', 4096, 'geometry') FROM "
            + "(SELECT id, properties, ST_AsMvtGeom(geometry, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845), 4096, 256, true) AS geometry "
            + "FROM (SELECT id, geom FROM ways WHERE tags -> 'building' = 'yes') AS layer "
            + "WHERE geometry && ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845) "
            + "AND ST_Intersects(geometry, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845))) as buildings, "
            + "(SELECT id, properties, ST_AsMvtGeom(geometry, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845), 4096, 256, true) AS geometry "
            + "FROM ( SELECT id, geom FROM ways WHERE tags -> 'highway' = 'path') AS layer "
            + "WHERE geometry && ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845) "
            + "AND ST_Intersects(geometry, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845))) as highways",
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
    assertEquals("ST_AsMVT(buildings, 'buildings', 4096, 'geometry')", sql);
  }

  @Test
  public void buildSources() {
    List<String> sources = PostgisQueryBuilder.buildSources(XYZ, LAYERS);
    assertNotNull(sources);
    assertTrue(sources.size() == 2);
  }

  @Test
  public void buildSource() {
    String sql = PostgisQueryBuilder.buildSource(XYZ, LAYER);
    assertEquals(
        "(SELECT id, properties, ST_AsMvtGeom(geometry, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845), 4096, 256, true) AS geometry "
            + "FROM (SELECT id, geom FROM ways WHERE tags -> 'building' = 'yes') AS layer "
            + "WHERE geometry && ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845) "
            + "AND ST_Intersects(geometry, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845))) as buildings",
        sql);
  }
}
