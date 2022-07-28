// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet6Address;

public class Inet6AddressValueHandler extends BaseValueHandler<Inet6Address> {

    private static final byte IPv6 = 3;
    private static final int MASK = 128;
    private static final byte IS_CIDR = 0;

    @Override
    protected void internalHandle(DataOutputStream buffer, final Inet6Address value) throws IOException {
        buffer.writeInt(20);

        buffer.writeByte(IPv6);
        buffer.writeByte(MASK);
        buffer.writeByte(IS_CIDR);

        byte[] inet6AddressBytes = value.getAddress();
        buffer.writeByte(inet6AddressBytes.length);
        buffer.write(inet6AddressBytes);
    }
}
