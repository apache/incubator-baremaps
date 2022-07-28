// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.model.Range;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RangeValueHandler<T> extends BaseValueHandler<Range<T>> {

    private final ValueHandler<T> valueHandler;

    public RangeValueHandler(ValueHandler<T> valueHandler) {
        this.valueHandler = valueHandler;
    }

    @Override
    protected void internalHandle(DataOutputStream buffer, Range<T> value) throws IOException {
        var length = 1;
        var bytes = new ByteArrayOutputStream();
        var output = new DataOutputStream(bytes);

        if(!value.isLowerBoundInfinite()) {
            length += 4;
            valueHandler.handle(output, value.getLowerBound());
        }

        if(!value.isUpperBoundInfinite()) {
            length += 4;
            valueHandler.handle(output, value.getUpperBound());
        }

        length += bytes.size();

        buffer.writeInt(length);
        buffer.writeByte(value.getFlags());

        if (value.isEmpty()) {
            return;
        }

        buffer.write(bytes.toByteArray());
    }
}

