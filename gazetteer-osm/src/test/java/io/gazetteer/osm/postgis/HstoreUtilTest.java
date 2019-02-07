package io.gazetteer.osm.postgis;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class HstoreUtilTest {

  @Test
  public void asHstore() {
    Map<String, String> m = new HashMap<>();
    m.put("key", "val");
    assertTrue(HstoreUtil.asHstore(m).length > 0);
  }

  @Test
  public void asMap() {
    Map<String, String> m1 = new HashMap<>();
    m1.put("key", "val");
    Map<String, String> m2 = HstoreUtil.asMap(HstoreUtil.asHstore(m1));
    assertEquals(m2.get("key"), m1.get("key"));
  }
}