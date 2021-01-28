package com.baremaps.util.tile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

class TileTest {

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