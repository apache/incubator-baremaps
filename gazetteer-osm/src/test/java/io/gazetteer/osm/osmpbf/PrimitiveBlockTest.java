package io.gazetteer.osm.osmpbf;

import io.gazetteer.osm.domain.Info;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class PrimitiveBlockTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Info.class).verify();
    }

}