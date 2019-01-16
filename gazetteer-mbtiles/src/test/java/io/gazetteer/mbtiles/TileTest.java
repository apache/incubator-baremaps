package io.gazetteer.mbtiles;

import org.junit.Test;

import static org.junit.Assert.*;

public class TileTest {

    @Test
    public void equalsTest() {
        Tile t1 = new Tile("a".getBytes());
        Tile t2 = new Tile("a".getBytes());
        Tile t3 = new Tile("b".getBytes());
        assertEquals(t1, t2);
        assertNotEquals(t1, t3);
    }

    @Test
    public void hashCodeTest() {
        Tile t1 = new Tile("a".getBytes());
        Tile t2 = new Tile("a".getBytes());
        Tile t3 = new Tile("b".getBytes());
        assertTrue(t1.hashCode() == t2.hashCode());
        assertFalse(t1.hashCode() == t3.hashCode());
    }
}