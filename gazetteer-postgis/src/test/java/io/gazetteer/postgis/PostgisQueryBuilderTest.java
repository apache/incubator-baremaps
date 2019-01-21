package io.gazetteer.postgis;

import io.gazetteer.core.XYZ;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PostgisQueryBuilderTest {

    private static final XYZ XYZ = new XYZ(8567, 5773, 14);

    private static final List<PostgisLayer> LAYERS = Arrays.asList(
            new PostgisLayer("buildings", "geom", 0, 20, "SELECT id, geom FROM ways WHERE tags -> 'building' = 'yes'"),
            new PostgisLayer("highways", "geom", 0, 20, " SELECT id, geom FROM ways WHERE tags -> 'highway' = 'path'")
    );

    private static final PostgisLayer LAYER = LAYERS.get(0);

    @Test
    public void buildLayer() {
        String sql = PostgisQueryBuilder.build(XYZ, LAYER);
        assertEquals(sql, "SELECT ST_AsMVT(buildings, 'buildings', 4096, 'geom') FROM " +
                "(SELECT id, ST_AsMvtGeom(geom, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845), 4096, 256, true) AS geom " +
                "FROM (SELECT id, geom FROM ways WHERE tags -> 'building' = 'yes') AS layer " +
                "WHERE geom && ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845) AND ST_Intersects(geom, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845))) as buildings");
    }

    @Test
    public void buildLayers() {
        String sql = PostgisQueryBuilder.build(XYZ, LAYERS);
        assertEquals(sql, "SELECT ST_AsMVT(buildings, 'buildings', 4096, 'geom') || ST_AsMVT(highways, 'highways', 4096, 'geom') FROM " +
                "(SELECT id, ST_AsMvtGeom(geom, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845), 4096, 256, true) AS geom " +
                "FROM (SELECT id, geom FROM ways WHERE tags -> 'building' = 'yes') AS layer " +
                "WHERE geom && ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845) AND ST_Intersects(geom, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845))) as buildings, " +
                "(SELECT id, ST_AsMvtGeom(geom, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845), 4096, 256, true) AS geom " +
                "FROM ( SELECT id, geom FROM ways WHERE tags -> 'highway' = 'path') AS layer " +
                "WHERE geom && ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845) AND ST_Intersects(geom, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845))) as highways");
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
        assertEquals(sql, "ST_AsMVT(buildings, 'buildings', 4096, 'geom')");
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
        assertEquals(sql,
                "(SELECT id, tags, ST_AsMvtGeom(geom, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845), 4096, 256, true) AS geom " +
                        "FROM (SELECT id, geom FROM ways WHERE tags -> 'building' = 'yes') AS layer " +
                        "WHERE geom && ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845) " +
                        "AND ST_Intersects(geom, ST_MakeEnvelope(8.24, 46.83, 8.262, 46.845))) as buildings");
    }
}