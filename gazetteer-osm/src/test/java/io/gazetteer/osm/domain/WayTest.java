package io.gazetteer.osm.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class WayTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Way.class).verify();
    }

}