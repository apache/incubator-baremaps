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

package com.baremaps.postgres.handlers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class UUIDValueHandler extends BaseValueHandler<UUID> {

  @Override
  protected void internalHandle(DataOutputStream buffer, final UUID value) throws IOException {
    buffer.writeInt(16);

    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(value.getMostSignificantBits());
    bb.putLong(value.getLeastSignificantBits());

    buffer.writeInt(bb.getInt(0));
    buffer.writeShort(bb.getShort(4));
    buffer.writeShort(bb.getShort(6));

    buffer.write(Arrays.copyOfRange(bb.array(), 8, 16));
  }
}
