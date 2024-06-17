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

import java.nio.ByteBuffer;

public class Constants {

  public static final byte[] MAGIC_BYTES =
      new byte[] {0x66, 0x67, 0x62, 0x03, 0x66, 0x67, 0x62, 0x00};

  public static boolean isFlatgeobuf(ByteBuffer bb) {
    return bb.get() == MAGIC_BYTES[0] &&
        bb.get() == MAGIC_BYTES[1] &&
        bb.get() == MAGIC_BYTES[2] &&
        bb.get() == MAGIC_BYTES[3] &&
        bb.get() == MAGIC_BYTES[4] &&
        bb.get() == MAGIC_BYTES[5] &&
        bb.get() == MAGIC_BYTES[6] &&
        bb.get() == MAGIC_BYTES[7];
  }
}
