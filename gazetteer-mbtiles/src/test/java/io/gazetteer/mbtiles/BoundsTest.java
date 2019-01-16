package io.gazetteer.mbtiles;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class BoundsTest {

    @Test
    public void serializeDeserialize() {
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            double left = random.nextDouble();
            double bottom = random.nextDouble();
            double right = left + 1;
            double top = bottom + 1;
            Bounds b1 = new Bounds(left, bottom, right, top);
            Bounds b2 = Bounds.deserialize(b1.serialize());
            boolean equality = b1.equals(b2);
            Assert.assertTrue(equality);
        }
    }
    @Test(expected = IllegalArgumentException.class)
    public void illegalArguments() {
        new Bounds(1, 1, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deserializeEmptyString() {
        Bounds.deserialize("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void deserializeIllegalString1() {
        Bounds.deserialize(",,,");
    }

    @Test(expected = IllegalArgumentException.class)
    public void deserializeIllegalString2() {
        Bounds.deserialize("0,,0,0");
    }

}
