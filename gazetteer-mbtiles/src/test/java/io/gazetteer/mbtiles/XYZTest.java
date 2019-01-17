package io.gazetteer.mbtiles;

import mil.nga.sf.GeometryEnvelope;
import nl.jqno.equalsverifier.EqualsVerifier;
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
    public void equalsContract() {
        EqualsVerifier.forClass(XYZ.class).verify();
    }

}