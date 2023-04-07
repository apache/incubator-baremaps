package org.apache.baremaps.collection.type.geometry;

import org.apache.baremaps.collection.type.DataType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.nio.ByteBuffer;

/**
 * A data type for {@link Point} objects.
 */
public class PointDataType implements DataType<Point> {

    private final GeometryFactory geometryFactory;

    /**
     * Constructs a {@code PointDataType} with a default {@code GeometryFactory}.
     */
    public PointDataType() {
        this(new GeometryFactory());
    }

    /**
     * Constructs a {@code PointDataType} with a specified {@code GeometryFactory}.
     *
     * @param geometryFactory the geometry factory
     */
    public PointDataType(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size(Point value) {
        return Double.BYTES * 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size(ByteBuffer buffer, int position) {
        return Double.BYTES * 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(ByteBuffer buffer, int position, Point value) {
        if (value.isEmpty()) {
            buffer.putDouble(position, Double.NaN);
            buffer.putDouble(position + Double.BYTES, Double.NaN);
        } else {
            buffer.putDouble(position, value.getX());
            buffer.putDouble(position + Double.BYTES, value.getY());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point read(ByteBuffer buffer, int position) {
        double x = buffer.getDouble(position);
        double y = buffer.getDouble(position + Double.BYTES);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return geometryFactory.createPoint();
        } else {
            return geometryFactory.createPoint(new Coordinate(x, y));
        }
    }
}
