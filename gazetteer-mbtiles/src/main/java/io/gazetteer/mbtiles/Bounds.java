package io.gazetteer.mbtiles;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Bounds {

    public final double left, bottom, right, top;

    public Bounds(double left, double bottom, double right, double top) {
        checkArgument(left < right, "Expected left < right, but %s >= %s", left, right);
        checkArgument(bottom < top, "Expected bottom < top, but %s >= %s", bottom, top);
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.top = top;
    }

    public String serialize() {
        return Joiner.on(",").join(Arrays.asList(left, bottom, right, top));
    }

    public static Bounds deserialize(String bounds) {
        checkNotNull(bounds);
        String[] arr = bounds.split(",");
        try {
            double left = Double.parseDouble(arr[0]);
            double bottom = Double.parseDouble(arr[1]);
            double right = Double.parseDouble(arr[2]);
            double top = Double.parseDouble(arr[3]);
            return new Bounds(left, bottom, right, top);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Invalid bounds: %s", bounds));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bounds bounds = (Bounds) o;
        return Double.compare(bounds.left, left) == 0 &&
                Double.compare(bounds.bottom, bottom) == 0 &&
                Double.compare(bounds.right, right) == 0 &&
                Double.compare(bounds.top, top) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(left, bottom, right, top);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("left", left)
                .add("bottom", bottom)
                .add("right", right)
                .add("top", top)
                .toString();
    }

}