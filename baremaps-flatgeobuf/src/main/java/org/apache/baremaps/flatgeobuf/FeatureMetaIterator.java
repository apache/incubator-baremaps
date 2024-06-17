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

//
// import java.io.IOException;
// import java.nio.ByteBuffer;
// import java.nio.channels.ReadableByteChannel;
// import java.nio.channels.SeekableByteChannel;
// import java.util.Iterator;
// import java.util.NoSuchElementException;
//
// public class FeatureMetaIterator implements Iterator<FeatureMeta> {
//
// private final HeaderMeta headerMeta;
//
// private final ReadableByteChannel channel;
//
// private final ByteBuffer buffer;
//
// private long cursor = 0;
//
// /**
// * Constructs a row iterator.
// *
// * @param channel the channel to read from
// * @param headerMeta the header meta
// * @param buffer the buffer to use
// */
// public FeatureMetaIterator(
// SeekableByteChannel channel,
// HeaderMeta headerMeta,
// ByteBuffer buffer) {
// this.channel = channel;
// this.headerMeta = headerMeta;
// this.buffer = buffer;
// }
//
// /**
// * {@inheritDoc}
// */
// @Override
// public boolean hasNext() {
// return cursor < headerMeta.featuresCount;
// }
//
// /**
// * {@inheritDoc}
// */
// @Override
// public FeatureMeta next() {
// try {
// channel.read(buffer);
// buffer.flip();
//
// var featureSize = buffer.getInt();
// var featureMeta = FlatGeoBufReader.readFeature(buffer, headerMeta);
//
// buffer.position(Integer.BYTES + featureSize);
// buffer.compact();
//
// cursor++;
//
// return featureMeta;
// } catch (IOException e) {
// throw new NoSuchElementException(e);
// }
// }
//
// }
