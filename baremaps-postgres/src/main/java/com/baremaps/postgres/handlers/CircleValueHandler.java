/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.postgres.handlers;

import com.baremaps.postgres.model.Circle;
import com.baremaps.postgres.util.GeometricUtils;
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
