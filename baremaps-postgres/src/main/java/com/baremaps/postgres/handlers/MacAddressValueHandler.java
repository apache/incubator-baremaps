// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.model.MacAddress;

import java.io.DataOutputStream;
import java.io.IOException;

public class MacAddressValueHandler extends BaseValueHandler<MacAddress> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final MacAddress value) throws IOException {
        buffer.writeInt(6);
        buffer.write(value.getAddressBytes());
    }
}
