// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import java.io.DataOutputStream;
import java.io.IOException;

public class DoubleValueHandler<T extends Number> extends BaseValueHandler<T> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final T value) throws IOException {
        buffer.writeInt(8);
        buffer.writeDouble(value.doubleValue());
    }
}
