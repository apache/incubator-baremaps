package com.baremaps.tile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

class TileTest {

  @Test
  void getTile() {
    double lon = 1062451.988597151, lat = 5965417.348546229;
    int z = 14;
    Tile tile = Tile.fromLonLat(lon, lat, 14);
    int y = (int) ((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat)))
        / Math.PI) / 2.0 * (1 << z));
    // TODO: test something ;)
  }

  @Test
  public void count() {
    Envelope envelope = new Tile(0, 0, 0).envelope();
    assertEquals(
        Tile.count(envelope, 0, 2),
        Tile.list(envelope, 0, 2).size());
  }

  @Test
  public void countLiechtenstein() {
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