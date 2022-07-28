// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.util.GeometricUtils;
import com.baremaps.postgres.model.Box;

import java.io.DataOutputStream;
import java.io.IOException;

public class BoxValueHandler extends BaseValueHandler<Box> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final Box value) throws IOException {
        buffer.writeInt(32);

        GeometricUtils.writePoint(buffer, value.getHigh());
        GeometricUtils.writePoint(buffer, value.getLow());
    }
}