package org.apache.baremaps.collection.type.geometry;

import org.apache.baremaps.collection.type.DataType;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.nio.ByteBuffer;

/**
 * A data type for {@link LineString} objects.
 */
public class LineStringDataType implements DataType<LineString> {

    private final GeometryFactory geometryFactory;

    private final CoordinateArrayDataType coordinateArrayDataType;

    /**
     * Constructs a {@code LineStringDataType} with a default {@code GeometryFactory}.
     */
    public LineStringDataType() {
        this(new GeometryFactory());
    }

    /**
     * Constructs a {@code LineStringDataType} with a specified {@code GeometryFactory}.
     *
     * @param geometryFactory the geometry factory
     */
    public LineStringDataType(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        this.coordinateArrayDataType = new CoordinateArrayDataType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size(LineString value) {
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
    public void write(ByteBuffer buffer, int position, LineString value) {
        coordinateArrayDataType.write(buffer, position, value.getCoordinates());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LineString read(ByteBuffer buffer, int position) {
        var coordinates = coordinateArrayDataType.read(buffer, position);
        return geometryFactory.createLineString(coordinates);
    }
}
