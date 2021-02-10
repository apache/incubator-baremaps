package com.baremaps.tile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

class TileIteratorTest {
  @Test
  public void iterator() {
    Envelope e = new Tile(0,0,0).envelope();

    TileIterator i0 = new TileIterator(e, 0, 0);
    List<Tile> l0 = ImmutableList.copyOf(i0);
    assertEquals(l0.size(), 1);

    TileIterator i1= new TileIterator(e, 1, 1);
    List<Tile> l1 = ImmutableList.copyOf(i1);
    assertEquals(l1.size(), 4);

    TileIterator i2= new TileIterator(e, 2, 2);
    List<Tile> l2 = ImmutableList.copyOf(i2);
    assertEquals(l2.size(), 16);
  }

  @Test
  public void partial() {
    Envelope e0 = new Tile(0,0,1).envelope();
    TileIterator i0 = new TileIterator(e0, 2, 2);
    List<Tile> l0 = ImmutableList.copyOf(i0);
    assertEquals(l0.size(), 4);

    Envelope e1 = new Tile(1,1,1).envelope();
    TileIterator i1 = new TileIterator(e1, 2, 2);
    List<Tile> l1 = ImmutableList.copyOf(i1);
    assertEquals(l1.size(), 4);
  }


}