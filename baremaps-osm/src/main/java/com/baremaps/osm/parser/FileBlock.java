/*
 * Copyright (C) 2020 The Baremaps Authors
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

package com.baremaps.osm.parser;

import com.google.protobuf.ByteString;

public class FileBlock {

  public enum Type {
    OSMHeader,
    OSMData
  }

  private final Type type;
  private final ByteString index;
  private final ByteString data;

  public FileBlock(Type type, ByteString index, ByteString data) {
    this.type = type;
    this.index = index;
    this.data = data;
  }

  public Type getType() {
    return type;
  }

  public ByteString getIndex() {
    return index;
  }

  public ByteString getData() {
    return data;
  }

}
