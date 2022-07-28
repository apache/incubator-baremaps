// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.util.GeometricUtils;
import com.baremaps.postgres.model.Circle;

import java.io.DataOutputStream;
import java.io.IOException;

public class CircleValueHandler extends BaseValueHandler<Circle> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final Circle value) throws IOException {
        buffer.writeInt(24);
        // First encode the Center Point:
        GeometricUtils.writePoint(buffer, value.getCenter());
        // ... and then the Radius:
        buffer.writeDouble(value.getRadius());
    }
}