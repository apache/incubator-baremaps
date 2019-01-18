package io.gazetteer.osm.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class NodeTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Node.class).verify();
    }

}