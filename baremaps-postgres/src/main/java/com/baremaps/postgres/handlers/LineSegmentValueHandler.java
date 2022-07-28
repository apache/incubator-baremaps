// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.util.GeometricUtils;
import com.baremaps.postgres.model.LineSegment;

import java.io.DataOutputStream;
import java.io.IOException;

public class LineSegmentValueHandler extends BaseValueHandler<LineSegment> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final LineSegment value) throws IOException {
        buffer.writeInt(32);

        GeometricUtils.writePoint(buffer, value.getP1());
        GeometricUtils.writePoint(buffer, value.getP2());
    }
}