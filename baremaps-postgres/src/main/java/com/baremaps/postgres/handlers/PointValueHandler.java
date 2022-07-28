// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.util.GeometricUtils;
import com.baremaps.postgres.model.Point;

import java.io.DataOutputStream;
import java.io.IOException;

public class PointValueHandler extends BaseValueHandler<Point> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final Point value) throws IOException {
        buffer.writeInt(16);

        GeometricUtils.writePoint(buffer, value);
    }
}