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

package org.apache.baremaps.flatgeobuf.generated;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.DoubleVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.IntVector;
import com.google.flatbuffers.LongVector;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Geometry extends Table {
  public static void ValidateVersion() {
    Constants.FLATBUFFERS_24_3_25();
  }

  public static Geometry getRootAsGeometry(ByteBuffer _bb) {
    return getRootAsGeometry(_bb, new Geometry());
  }

  public static Geometry getRootAsGeometry(ByteBuffer _bb, Geometry obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
  }

  public void __init(int _i, ByteBuffer _bb) {
    __reset(_i, _bb);
  }

  public Geometry __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }

  public long ends(int j) {
    int o = __offset(4);
    return o != 0 ? (long) bb.getInt(__vector(o) + j * 4) & 0xFFFFFFFFL : 0;
  }

  public int endsLength() {
    int o = __offset(4);
    return o != 0 ? __vector_len(o) : 0;
  }

  public IntVector endsVector() {
    return endsVector(new IntVector());
  }

  public IntVector endsVector(IntVector obj) {
    int o = __offset(4);
    return o != 0 ? obj.__assign(__vector(o), bb) : null;
  }

  public ByteBuffer endsAsByteBuffer() {
    return __vector_as_bytebuffer(4, 4);
  }

  public ByteBuffer endsInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 4, 4);
  }

  public double xy(int j) {
    int o = __offset(6);
    return o != 0 ? bb.getDouble(__vector(o) + j * 8) : 0;
  }

  public int xyLength() {
    int o = __offset(6);
    return o != 0 ? __vector_len(o) : 0;
  }

  public DoubleVector xyVector() {
    return xyVector(new DoubleVector());
  }

  public DoubleVector xyVector(DoubleVector obj) {
    int o = __offset(6);
    return o != 0 ? obj.__assign(__vector(o), bb) : null;
  }

  public ByteBuffer xyAsByteBuffer() {
    return __vector_as_bytebuffer(6, 8);
  }

  public ByteBuffer xyInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 6, 8);
  }

  public double z(int j) {
    int o = __offset(8);
    return o != 0 ? bb.getDouble(__vector(o) + j * 8) : 0;
  }

  public int zLength() {
    int o = __offset(8);
    return o != 0 ? __vector_len(o) : 0;
  }

  public DoubleVector zVector() {
    return zVector(new DoubleVector());
  }

  public DoubleVector zVector(DoubleVector obj) {
    int o = __offset(8);
    return o != 0 ? obj.__assign(__vector(o), bb) : null;
  }

  public ByteBuffer zAsByteBuffer() {
    return __vector_as_bytebuffer(8, 8);
  }

  public ByteBuffer zInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 8, 8);
  }

  public double m(int j) {
    int o = __offset(10);
    return o != 0 ? bb.getDouble(__vector(o) + j * 8) : 0;
  }

  public int mLength() {
    int o = __offset(10);
    return o != 0 ? __vector_len(o) : 0;
  }

  public DoubleVector mVector() {
    return mVector(new DoubleVector());
  }

  public DoubleVector mVector(DoubleVector obj) {
    int o = __offset(10);
    return o != 0 ? obj.__assign(__vector(o), bb) : null;
  }

  public ByteBuffer mAsByteBuffer() {
    return __vector_as_bytebuffer(10, 8);
  }

  public ByteBuffer mInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 10, 8);
  }

  public double t(int j) {
    int o = __offset(12);
    return o != 0 ? bb.getDouble(__vector(o) + j * 8) : 0;
  }

  public int tLength() {
    int o = __offset(12);
    return o != 0 ? __vector_len(o) : 0;
  }

  public DoubleVector tVector() {
    return tVector(new DoubleVector());
  }

  public DoubleVector tVector(DoubleVector obj) {
    int o = __offset(12);
    return o != 0 ? obj.__assign(__vector(o), bb) : null;
  }

  public ByteBuffer tAsByteBuffer() {
    return __vector_as_bytebuffer(12, 8);
  }

  public ByteBuffer tInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 12, 8);
  }

  public long tm(int j) {
    int o = __offset(14);
    return o != 0 ? bb.getLong(__vector(o) + j * 8) : 0;
  }

  public int tmLength() {
    int o = __offset(14);
    return o != 0 ? __vector_len(o) : 0;
  }

  public LongVector tmVector() {
    return tmVector(new LongVector());
  }

  public LongVector tmVector(LongVector obj) {
    int o = __offset(14);
    return o != 0 ? obj.__assign(__vector(o), bb) : null;
  }

  public ByteBuffer tmAsByteBuffer() {
    return __vector_as_bytebuffer(14, 8);
  }

  public ByteBuffer tmInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 14, 8);
  }

  public int type() {
    int o = __offset(16);
    return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0;
  }

  public Geometry parts(int j) {
    return parts(new Geometry(), j);
  }

  public Geometry parts(Geometry obj, int j) {
    int o = __offset(18);
    return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null;
  }

  public int partsLength() {
    int o = __offset(18);
    return o != 0 ? __vector_len(o) : 0;
  }

  public Geometry.Vector partsVector() {
    return partsVector(new Geometry.Vector());
  }

  public Geometry.Vector partsVector(Geometry.Vector obj) {
    int o = __offset(18);
    return o != 0 ? obj.__assign(__vector(o), 4, bb) : null;
  }

  public static int createGeometry(FlatBufferBuilder builder,
      int endsOffset,
      int xyOffset,
      int zOffset,
      int mOffset,
      int tOffset,
      int tmOffset,
      int type,
      int partsOffset) {
    builder.startTable(8);
    Geometry.addParts(builder, partsOffset);
    Geometry.addTm(builder, tmOffset);
    Geometry.addT(builder, tOffset);
    Geometry.addM(builder, mOffset);
    Geometry.addZ(builder, zOffset);
    Geometry.addXy(builder, xyOffset);
    Geometry.addEnds(builder, endsOffset);
    Geometry.addType(builder, type);
    return Geometry.endGeometry(builder);
  }

  public static void startGeometry(FlatBufferBuilder builder) {
    builder.startTable(8);
  }

  public static void addEnds(FlatBufferBuilder builder, int endsOffset) {
    builder.addOffset(0, endsOffset, 0);
  }

  public static int createEndsVector(FlatBufferBuilder builder, long[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; i--)
      builder.addInt((int) data[i]);
    return builder.endVector();
  }

  public static void startEndsVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }

  public static void addXy(FlatBufferBuilder builder, int xyOffset) {
    builder.addOffset(1, xyOffset, 0);
  }

  public static int createXyVector(FlatBufferBuilder builder, double[] data) {
    builder.startVector(8, data.length, 8);
    for (int i = data.length - 1; i >= 0; i--)
      builder.addDouble(data[i]);
    return builder.endVector();
  }

  public static void startXyVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(8, numElems, 8);
  }

  public static void addZ(FlatBufferBuilder builder, int zOffset) {
    builder.addOffset(2, zOffset, 0);
  }

  public static int createZVector(FlatBufferBuilder builder, double[] data) {
    builder.startVector(8, data.length, 8);
    for (int i = data.length - 1; i >= 0; i--)
      builder.addDouble(data[i]);
    return builder.endVector();
  }

  public static void startZVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(8, numElems, 8);
  }

  public static void addM(FlatBufferBuilder builder, int mOffset) {
    builder.addOffset(3, mOffset, 0);
  }

  public static int createMVector(FlatBufferBuilder builder, double[] data) {
    builder.startVector(8, data.length, 8);
    for (int i = data.length - 1; i >= 0; i--)
      builder.addDouble(data[i]);
    return builder.endVector();
  }

  public static void startMVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(8, numElems, 8);
  }

  public static void addT(FlatBufferBuilder builder, int tOffset) {
    builder.addOffset(4, tOffset, 0);
  }

  public static int createTVector(FlatBufferBuilder builder, double[] data) {
    builder.startVector(8, data.length, 8);
    for (int i = data.length - 1; i >= 0; i--)
      builder.addDouble(data[i]);
    return builder.endVector();
  }

  public static void startTVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(8, numElems, 8);
  }

  public static void addTm(FlatBufferBuilder builder, int tmOffset) {
    builder.addOffset(5, tmOffset, 0);
  }

  public static int createTmVector(FlatBufferBuilder builder, long[] data) {
    builder.startVector(8, data.length, 8);
    for (int i = data.length - 1; i >= 0; i--)
      builder.addLong(data[i]);
    return builder.endVector();
  }

  public static void startTmVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(8, numElems, 8);
  }

  public static void addType(FlatBufferBuilder builder, int type) {
    builder.addByte(6, (byte) type, (byte) 0);
  }

  public static void addParts(FlatBufferBuilder builder, int partsOffset) {
    builder.addOffset(7, partsOffset, 0);
  }

  public static int createPartsVector(FlatBufferBuilder builder, int[] data) {
    builder.startVector(4, data.length, 4);
    for (int i = data.length - 1; i >= 0; i--)
      builder.addOffset(data[i]);
    return builder.endVector();
  }

  public static void startPartsVector(FlatBufferBuilder builder, int numElems) {
    builder.startVector(4, numElems, 4);
  }

  public static int endGeometry(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
      __reset(_vector, _element_size, _bb);
      return this;
    }

    public Geometry get(int j) {
      return get(new Geometry(), j);
    }

    public Geometry get(Geometry obj, int j) {
      return obj.__assign(__indirect(__element(j), bb), bb);
    }
  }
}