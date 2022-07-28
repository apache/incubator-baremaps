// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.util;

import com.baremaps.postgres.model.Point;

import java.io.DataOutputStream;
import java.io.IOException;

public class GeometricUtils {

    public static void writePoint(DataOutputStream buffer, final Point value) throws IOException {
        buffer.writeDouble(value.getX());
        buffer.writeDouble(value.getY());
    }

}
