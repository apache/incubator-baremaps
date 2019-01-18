package io.gazetteer.osm.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class RelationTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Relation.class).verify();
    }

}