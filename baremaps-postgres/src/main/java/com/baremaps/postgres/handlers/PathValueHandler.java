// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.util.GeometricUtils;
import com.baremaps.postgres.model.Path;
import com.baremaps.postgres.model.Point;

import java.io.DataOutputStream;
import java.io.IOException;

public class PathValueHandler extends BaseValueHandler<Path> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final Path value) throws IOException {
        // Write a Byte to indicate if a Path is closed or not:
        byte pathIsClosed = (byte) (value.isClosed() ? 1 : 0);

        // The total number of bytes to write:
        int totalBytesToWrite = 1 + 4 + 16 * value.size();

        // The Number of Bytes to follow:
        buffer.writeInt(totalBytesToWrite);
        // Is the Circle close?
        buffer.writeByte(pathIsClosed);
        // Write Points:
        buffer.writeInt(value.getPoints().size());
        // Write each Point in List:
        for (Point p : value.getPoints()) {
            GeometricUtils.writePoint(buffer, p);
        }

    }
}