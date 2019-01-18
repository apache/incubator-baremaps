package io.gazetteer.osm.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class MemberTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Member.class).verify();
    }

}