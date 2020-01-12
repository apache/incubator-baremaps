package io.gazetteer.tiles.postgis;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.gazetteer.tiles.config.Config;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

public class ConfigTest {

  @Test
  public void load() {
    InputStream input = this.getClass().getClassLoader().getResourceAsStream("config.yaml");
    Config config = Config.load(input);
    assertNotNull(config);
    assertTrue(config.getLayers().size() == 2);
  }
}
