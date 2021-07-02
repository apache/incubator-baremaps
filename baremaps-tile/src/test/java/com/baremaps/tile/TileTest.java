package com.baremaps.tile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

class TileTest {

  @Test
  void getTile() {
    double lon = 0, lat = 0;
    for (int z = 0; z <= 14; z +=1) {
      Tile tile = Tile.fromLonLat(lon, lat, z);
      int value = (int) Math.pow(2, z-1);
      assertEquals(value, tile.x());
      assertEquals(value, tile.y());
    }
  }

  @Test
  void count() {
    Envelope envelope = new Tile(0, 0, 0).envelope();
    assertEquals(
        Tile.count(envelope, 0, 2),
        Tile.list(envelope, 0, 2).size());
  }

  @Test
  void countLiechtenstein() {
    double minLon = 9.471078;
    double maxLon = 9.636217;
    double minLat = 47.04774;
    double maxLat = 47.27128;
    int minZoom = 12;
    int maxZoom = 14;
    Envelope envelope = new Envelope(minLon, maxLon, minLat, maxLat);
    assertEquals(
        Tile.count(envelope, minZoom, maxZoom),
        Tile.list(envelope, minZoom, maxZoom).size());
  }

}