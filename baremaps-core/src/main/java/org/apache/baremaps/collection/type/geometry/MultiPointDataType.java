package org.apache.baremaps.collection.type.geometry;

import org.apache.baremaps.collection.type.DataType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;

import java.nio.ByteBuffer;

/**
 * A data type for {@link MultiPoint} objects.
 */
public class MultiPointDataType implements DataType<MultiPoint> {

    private final CoordinateArrayDataType coordinateArrayDataType = new CoordinateArrayDataType();

    private final GeometryFactory geometryFactory;

    /**
     * Constructs a {@code MultiPointDataType} with a default {@code GeometryFactory}.
     */
    public MultiPointDataType() {
        this(new GeometryFactory());
    }

    /**
     * Constructs a {@code MultiPointDataType} with a specified {@code GeometryFactory}.
     *
     * @param geometryFactory the geometry factory
     */
    public MultiPointDataType(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size(MultiPoint value) {
        return coordinateArrayDataType.size(value.getCoordinates());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size(ByteBuffer buffer, int position) {
        return coordinateArrayDataType.size(buffer, position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(ByteBuffer buffer, int position, MultiPoint value) {
        coordinateArrayDataType.write(buffer, position, value.getCoordinates());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiPoint read(ByteBuffer buffer, int position) {
        var coordinates = coordinateArrayDataType.read(buffer, position);
        return geometryFactory.createMultiPoint(coordinates);
    }
}
