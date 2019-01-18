package io.gazetteer.osm.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class UserTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(User.class).verify();
    }

}