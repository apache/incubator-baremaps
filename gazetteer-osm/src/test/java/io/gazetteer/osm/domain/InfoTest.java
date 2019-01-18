package io.gazetteer.osm.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class InfoTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Info.class).verify();
    }

}