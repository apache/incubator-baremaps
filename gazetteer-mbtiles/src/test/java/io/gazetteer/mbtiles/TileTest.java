package io.gazetteer.mbtiles;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.junit.Assert.*;

public class TileTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Tile.class).verify();
    }

}