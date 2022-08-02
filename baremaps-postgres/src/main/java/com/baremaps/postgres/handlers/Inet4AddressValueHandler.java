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
import java.net.Inet4Address;

public class Inet4AddressValueHandler extends BaseValueHandler<Inet4Address> {

  private static final byte IPv4 = 2;
  private static final byte MASK = 32;
  private static final byte IS_CIDR = 0;

  @Override
  protected void internalHandle(DataOutputStream buffer, final Inet4Address value)
      throws IOException {
    buffer.writeInt(8);

    buffer.writeByte(IPv4);
    buffer.writeByte(MASK);
    buffer.writeByte(IS_CIDR);

    byte[] inet4AddressBytes = value.getAddress();

    buffer.writeByte(inet4AddressBytes.length);
    buffer.write(inet4AddressBytes);
  }
}
