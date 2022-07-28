// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import java.io.DataOutputStream;

import com.baremaps.postgres.util.StringUtils;
import java.io.IOException;

public class StringValueHandler extends BaseValueHandler<String> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final String value) throws IOException {
        byte[] utf8Bytes = StringUtils.getUtf8Bytes(value);

        buffer.writeInt(utf8Bytes.length);
        buffer.write(utf8Bytes);
    }
}
