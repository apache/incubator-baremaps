package org.apache.baremaps.collection.type.geometry;

import org.apache.baremaps.collection.type.DataType;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * A data type for {@link MultiLineString} objects.
 */
public class MultiLineStringDataType implements DataType<MultiLineString> {

    private final LineStringDataType lineStringDataType;

    private final GeometryFactory geometryFactory;

    /**
     * Constructs a {@code MultiLineStringDataType} with a default {@code GeometryFactory}.
     */
    public MultiLineStringDataType() {
        this(new GeometryFactory());
    }

    /**
     * Constructs a {@code MultiLineStringDataType} with a specified {@code GeometryFactory}.
     *
     * @param geometryFactory the geometry factory
     */
    public MultiLineStringDataType(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        this.lineStringDataType = new LineStringDataType(geometryFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size(MultiLineString value) {
        int size = Integer.BYTES;
        for (int i = 0; i < value.getNumGeometries(); i++) {
            size += lineStringDataType.size((LineString) value.getGeometryN(i));
        }
        return size;
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
    public void write(ByteBuffer buffer, int position, MultiLineString value) {
        buffer.putInt(position, size(value));
        position += Integer.BYTES;
        for (int i = 0; i < value.getNumGeometries(); i++) {
            lineStringDataType.write(buffer, position, (LineString) value.getGeometryN(i));
            position += buffer.getInt(position);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiLineString read(ByteBuffer buffer, int position) {
        int size = buffer.getInt(position);
        position += Integer.BYTES;
        var lineStrings = new ArrayList<LineString>();
        while (position < size) {
            var lineString = lineStringDataType.read(buffer, position);
            lineStrings.add(lineString);
            position += lineStringDataType.size(buffer, position);
        }
        return geometryFactory.createMultiLineString(lineStrings.toArray(LineString[]::new));
    }
}
