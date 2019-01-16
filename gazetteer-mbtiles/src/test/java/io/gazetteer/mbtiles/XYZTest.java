package io.gazetteer.mbtiles;

import mil.nga.sf.GeometryEnvelope;
import org.junit.Test;

import static org.junit.Assert.*;

public class XYZTest {

    @Test
    public void envelope() {
        GeometryEnvelope e1 = new XYZ(0,0,2).envelope();
        GeometryEnvelope e2 = new XYZ(1,1,2).envelope();
        assertTrue(e1.getMaxX() == e2.getMinX());
        assertTrue(e1.getMinY() == e2.getMaxY());
    }

    @Test
    public void equalsTest() {
        XYZ c1 = new XYZ(0,0,0);
        XYZ c2 = new XYZ(0,0,0);
        XYZ c3 = new XYZ(1,0,0);
        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
    }

    @Test
    public void hashCodeTest() {
        XYZ c1 = new XYZ(0,0,0);
        XYZ c2 = new XYZ(0,0,0);
        XYZ c3 = new XYZ(1,0,0);
        assertTrue(c1.hashCode() == c2.hashCode());
        assertFalse(c1.hashCode() == c3.hashCode());
    }
}