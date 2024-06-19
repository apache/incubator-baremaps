/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.flatgeobuf;


import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.baremaps.flatgeobuf.generated.Column;
import org.apache.baremaps.flatgeobuf.generated.Crs;
import org.apache.baremaps.flatgeobuf.generated.Feature;
import org.apache.baremaps.flatgeobuf.generated.Header;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

public class FlatGeoBufWriter {

    public static void writeHeader(WritableByteChannel channel, Header header) throws IOException {
        ByteBuffer headerBuffer = header.getByteBuffer();
        ByteBuffer startBuffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
        startBuffer.put(FlatGeoBuf.MAGIC_BYTES);
        startBuffer.putInt(headerBuffer.remaining());
        startBuffer.flip();
        while (startBuffer.hasRemaining()) {
            channel.write(startBuffer);
        }
        while (headerBuffer.hasRemaining()) {
            channel.write(headerBuffer);
        }
    }

    public static void writeIndexStream(WritableByteChannel channel, InputStream inputStream) throws IOException {
        try (OutputStream outputStream = Channels.newOutputStream(channel)) {
            outputStream.write(inputStream.readAllBytes());
        }
    }

    public static void writeIndexBuffer(WritableByteChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public static void writeFeature(WritableByteChannel channel, Feature feature) throws IOException {
        ByteBuffer featureBuffer = feature.getByteBuffer().duplicate();
        featureBuffer.flip();
        channel.write(featureBuffer);
        while (featureBuffer.hasRemaining()) {
            channel.write(featureBuffer);
        }
    }

    public static void writeColumnValue(ByteBuffer buffer, FlatGeoBuf.Column column, Object value) {
        switch (column.type()) {
            case BYTE -> buffer.put((byte) value);
            case BOOL -> buffer.put((byte) ((boolean) value ? 1 : 0));
            case SHORT -> buffer.putShort((short) value);
            case INT -> buffer.putInt((int) value);
            case LONG -> buffer.putLong((long) value);
            case FLOAT -> buffer.putFloat((float) value);
            case DOUBLE -> buffer.putDouble((double) value);
            case STRING -> writeColumnString(buffer, value);
            case JSON -> writeColumnJson(buffer, value);
            case DATETIME -> writeColumnDateTime(buffer, value);
            case BINARY -> writeColumnBinary(buffer, value);
        }
    }

    public static void writeColumnString(ByteBuffer propertiesBuffer, Object value) {
        var bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
        propertiesBuffer.putInt(bytes.length);
        propertiesBuffer.put(bytes);
    }

    public static void writeColumnJson(ByteBuffer propertiesBuffer, Object value) {
        throw new UnsupportedOperationException();
    }

    public static void writeColumnDateTime(ByteBuffer propertiesBuffer, Object value) {
        throw new UnsupportedOperationException();
    }

    public static void writeColumnBinary(ByteBuffer propertiesBuffer, Object value) {
        throw new UnsupportedOperationException();
    }

    public static void writeFeature(
            OutputStream outputStream, FlatGeoBuf.Header headerMeta,
            FlatGeoBuf.Feature featureMeta) throws IOException {
        var featureBuilder = new FlatBufferBuilder(4096);

        // Write the properties
        var propertiesBuffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
        var properties = featureMeta.properties();
        for (int i = 0; i < properties.size(); i++) {
            var column = headerMeta.columns().get(i);
            var value = properties.get(i);
            propertiesBuffer.putShort((short) i);
            writeColumnValue(propertiesBuffer, column, value);
        }
        if (propertiesBuffer.position() > 0) {
            propertiesBuffer.flip();
        }
        var propertiesOffset = Feature.createPropertiesVector(featureBuilder, propertiesBuffer);

        // Write the geometry
        var geometry = featureMeta.geometry();
        var geometryOffset = 0;
        if (geometry != null) {
            geometryOffset =
                    GeometryConversions.writeGeometry(featureBuilder, geometry, (byte) headerMeta.geometryType().getValue());
        }

        // Write the feature
        var featureOffset = Feature.createFeature(featureBuilder, geometryOffset, propertiesOffset, 0);
        featureBuilder.finishSizePrefixed(featureOffset);

        byte[] data = featureBuilder.sizedByteArray();
        outputStream.write(data);
    }

    public static void write(FlatGeoBuf.Header headerMeta, OutputStream to, FlatBufferBuilder builder)
            throws IOException {
        int[] columnsArray = headerMeta.columns().stream().mapToInt(c -> {
            int nameOffset = builder.createString(c.name());
            int type = c.type().ordinal();
            return Column.createColumn(
                    builder,
                    nameOffset,
                    type,
                    0,
                    0,
                    c.width(),
                    c.precision(),
                    c.scale(),
                    c.nullable(),
                    c.unique(),
                    c.primaryKey(),
                    0);
        }).toArray();
        int columnsOffset = Header.createColumnsVector(builder, columnsArray);

        int nameOffset = 0;
        if (headerMeta.name() != null) {
            nameOffset = builder.createString(headerMeta.name());
        }
        int crsOffset = 0;
        if (headerMeta.crs().code() != 0) {
            Crs.startCrs(builder);
            Crs.addCode(builder, headerMeta.crs().code());
            crsOffset = Crs.endCrs(builder);
        }
        int envelopeOffset = 0;
        if (headerMeta.envelope() != null) {
            envelopeOffset = Header.createEnvelopeVector(builder,headerMeta.envelope().stream().mapToDouble(d -> d).toArray());
        }
        Header.startHeader(builder);
        Header.addGeometryType(builder, headerMeta.geometryType().getValue());
        Header.addIndexNodeSize(builder, headerMeta.indexNodeSize());
        Header.addColumns(builder, columnsOffset);
        Header.addEnvelope(builder, envelopeOffset);
        Header.addName(builder, nameOffset);
        Header.addCrs(builder, crsOffset);
        Header.addFeaturesCount(builder, headerMeta.featuresCount());
        int offset = Header.endHeader(builder);

        builder.finishSizePrefixed(offset);

        WritableByteChannel channel = Channels.newChannel(to);
        ByteBuffer dataBuffer = builder.dataBuffer();
        while (dataBuffer.hasRemaining()) {
            channel.write(dataBuffer);
        }
    }
}
