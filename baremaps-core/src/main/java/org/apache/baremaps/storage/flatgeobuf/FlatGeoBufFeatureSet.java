/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.storage.flatgeobuf;

import com.google.flatbuffers.FlatBufferBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.baremaps.collection.DataCollection;
import org.apache.baremaps.feature.Feature;
import org.apache.baremaps.feature.FeatureType;
import org.locationtech.jts.geom.Geometry;
import org.wololo.flatgeobuf.Constants;
import org.wololo.flatgeobuf.GeometryConversions;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.PackedRTree;
import org.wololo.flatgeobuf.generated.GeometryType;

public class FlatGeoBufFeatureSet {

    private final SeekableByteChannel channel;

    private HeaderMeta headerMeta;

    private FeatureType featureType;

    public FlatGeoBufFeatureSet(SeekableByteChannel channel) {
        this.channel = channel;
    }

    public FlatGeoBufFeatureSet(SeekableByteChannel channel, FeatureType featureType) {
        this.channel = channel;
        this.featureType = featureType;
    }

    public FeatureType getType() throws IOException {
        readHeaderMeta();
        return featureType;
    }

    public Collection<Feature> read() throws IOException {
        readHeaderMeta();

        return new DataCollection<>() {

            @Override
            public Iterator<Feature> iterator() {
                try {
                    channel.position(headerMeta.offset);

                    // skip the index
                    var indexSize =
                            (int) PackedRTree.calcSize((int) headerMeta.featuresCount, headerMeta.indexNodeSize);
                    channel.position(headerMeta.offset + indexSize);

                    // create the feature stream
                    return new FeatureIterator(channel, headerMeta, featureType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public long sizeAsLong() {
                return headerMeta.featuresCount;
            }

        };
    }

    public void readHeaderMeta() throws IOException {
        channel.position(0);
        var headerMetaBuffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
        channel.read(headerMetaBuffer);
        headerMetaBuffer.flip();
        headerMeta = HeaderMeta.read(headerMetaBuffer);
        featureType = FeatureConversions.asFeatureType(headerMeta);
    }

    public void write(Collection<Feature> features) throws IOException {
        channel.position(0);

        var outputStream = Channels.newOutputStream(channel);

        outputStream.write(Constants.MAGIC_BYTES);

        var bufferBuilder = new FlatBufferBuilder();

        var headerMeta = new HeaderMeta();
        headerMeta.geometryType = GeometryType.Unknown;
        headerMeta.indexNodeSize = 16;
        headerMeta.featuresCount = features instanceof DataCollection<Feature> c ? c.sizeAsLong() : features.size();
        headerMeta.name = featureType.getName();
        headerMeta.columns = FeatureConversions.asColumns(featureType.getPropertyTypes());

        HeaderMeta.write(headerMeta, outputStream, bufferBuilder);

        var indexSize =
                (int) PackedRTree.calcSize((int) headerMeta.featuresCount, headerMeta.indexNodeSize);

        for (int i = 0; i < indexSize; i++) {
            outputStream.write(0);
        }

        var iterator = features.iterator();
        while (iterator.hasNext()) {
            var feature = iterator.next();

            var featureBuilder = new FlatBufferBuilder();
            var geometryOffset = 0;
            var propertiesOffset = 0;
            var propertiesBuffer = ByteBuffer.allocate(1 << 20).order(ByteOrder.LITTLE_ENDIAN);
            var i = 0;
            for (Entry<String, Object> entry : feature.getProperties().entrySet()) {
                if (entry.getValue() instanceof Geometry) {
                    try {
                        var geometry = (Geometry) entry.getValue();
                        geometryOffset = GeometryConversions.serialize(featureBuilder, geometry, headerMeta.geometryType);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    var column = headerMeta.columns.get(i);
                    var value = feature.getProperty(column.name);
                    propertiesBuffer.putShort((short) i);
                    FeatureConversions.writeValue(propertiesBuffer, column, value);
                    i++;
                }
            }
            propertiesBuffer.flip();
            propertiesOffset = org.wololo.flatgeobuf.generated.Feature.createPropertiesVector(featureBuilder, propertiesBuffer);

            var featureOffset =
                    org.wololo.flatgeobuf.generated.Feature.createFeature(featureBuilder, geometryOffset, propertiesOffset, 0);

            featureBuilder.finishSizePrefixed(featureOffset);

            channel.write(featureBuilder.dataBuffer());
        }
    }
}
