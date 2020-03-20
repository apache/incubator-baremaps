/*
 * Copyright (C) 2011 The Baremaps Authors
 *
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

package com.baremaps.osm.osmpbf;

import static com.google.common.base.Preconditions.checkNotNull;

import com.baremaps.core.stream.StreamException;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.baremaps.osm.binary.Osmformat;

public final class FileBlock {

  private final Type type;
  private final ByteString indexdata;
  private final ByteString data;

  public FileBlock(Type type, ByteString indexdata, ByteString blob) {
    checkNotNull(type);
    checkNotNull(indexdata);
    checkNotNull(blob);
    this.type = type;
    this.indexdata = indexdata;
    this.data = blob;
  }

  public Type getType() {
    return type;
  }

  public boolean isHeaderBlock() {
    return getType() == Type.OSMHeader;
  }

  public boolean isPrimitiveBlock() {
    return getType() == Type.OSMData;
  }

  public HeaderBlock toHeaderBlock() {
    try {
      return new HeaderBlock(Osmformat.HeaderBlock.parseFrom(getData()));
    } catch (InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }

  public PrimitiveBlock toPrimitiveBlock() {
    try {
      return new PrimitiveBlock(Osmformat.PrimitiveBlock.parseFrom(getData()));
    } catch (InvalidProtocolBufferException e) {
      throw new StreamException(e);
    }
  }

  public ByteString getIndexdata() {
    return indexdata;
  }

  public ByteString getData() {
    return data;
  }

  public enum Type {
    OSMHeader,
    OSMData
  }
}
