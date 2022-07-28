// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.model.Line;

import java.io.DataOutputStream;
import java.io.IOException;

public class LineValueHandler extends BaseValueHandler<Line> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final Line value) throws IOException {
        buffer.writeInt(24);

        buffer.writeDouble(value.getA());
        buffer.writeDouble(value.getB());
        buffer.writeDouble(value.getC());
    }
}