package org.apache.baremaps.collection.type.geometry;

import org.apache.baremaps.collection.type.DataType;
import org.locationtech.jts.geom.Coordinate;

import java.nio.ByteBuffer;

/**
 * A data type for {@link Coordinate} arrays.
 */
public class CoordinateArrayDataType implements DataType<Coordinate[]> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int size(Coordinate[] value) {
        return Integer.BYTES + Double.BYTES * 2 * value.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size(ByteBuffer buffer, int position) {
        return buffer.getInt(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(ByteBuffer buffer, int position, Coordinate[] value) {
        buffer.putInt(position, size(value));
        position += Integer.BYTES;
        for (int i = 0; i < value.length; i++) {
            Coordinate coordinate = value[i];
            buffer.putDouble(position, coordinate.x);
            position += Double.BYTES;
            buffer.putDouble(position, coordinate.y);
            position += Double.BYTES;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Coordinate[] read(ByteBuffer buffer, int position) {
        int size = buffer.getInt(position);
        int numPoints = (size - Integer.BYTES) / (Double.BYTES * 2);
        position += Integer.BYTES;
        Coordinate[] coordinates = new Coordinate[numPoints];
        for (int i = 0; i < numPoints; i++) {
            double x = buffer.getDouble(position);
            double y = buffer.getDouble(position + Double.BYTES);
            coordinates[i] = new Coordinate(x, y);
            position += Double.BYTES * 2;
        }
        return coordinates;
    }
}
