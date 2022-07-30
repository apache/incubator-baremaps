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

import com.baremaps.postgres.model.Path;
import com.baremaps.postgres.model.Point;
import com.baremaps.postgres.util.GeometricUtils;
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
