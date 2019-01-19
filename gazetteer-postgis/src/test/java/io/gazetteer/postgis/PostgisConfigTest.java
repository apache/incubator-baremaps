package io.gazetteer.postgis;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

public class PostgisConfigTest {

    @Test
    public void load() {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("config.yaml");
        PostgisConfig config = PostgisConfig.load(input);
        assertNotNull(config);
        assertTrue(config.getLayers().size() == 2);
    }

}