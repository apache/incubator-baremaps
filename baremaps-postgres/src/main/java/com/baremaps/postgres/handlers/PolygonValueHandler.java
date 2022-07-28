// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.util.GeometricUtils;
import com.baremaps.postgres.model.Point;
import com.baremaps.postgres.model.Polygon;

import java.io.DataOutputStream;
import java.io.IOException;

public class PolygonValueHandler extends BaseValueHandler<Polygon> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final Polygon value) throws IOException {
        // The total number of bytes to write:
        int totalBytesToWrite = 4 + 16 * value.size();

        // The Number of Bytes to follow:
        buffer.writeInt(totalBytesToWrite);

        // Write Points:
        buffer.writeInt(value.getPoints().size());

        // Write each Point in List:
        for (Point p : value.getPoints()) {
            GeometricUtils.writePoint(buffer, p);
        }
    }
}