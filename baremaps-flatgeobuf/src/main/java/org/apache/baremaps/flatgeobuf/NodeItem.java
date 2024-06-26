/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.flatgeobuf;

import org.locationtech.jts.geom.Envelope;

/**
 * This code has been adapted from FlatGeoBuf (BSD 2-Clause "Simplified" License).
 * <p>
 * Copyright (c) 2018, Björn Harrtell
 */
public record NodeItem(
    double minX,
    double minY,
    double maxX,
    double maxY,
    long offset) {

  public NodeItem(double minX, double minY, double maxX, double maxY) {
    this(minX, minY, maxX, maxY, 0);
  }

  public NodeItem(long offset) {
    this(
        Double.POSITIVE_INFINITY,
        Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        offset);
  }

  public double width() {
    return maxX - minX;
  }

  public double height() {
    return maxY - minY;
  }

  public static NodeItem sum(NodeItem a, final NodeItem b) {
    return a.expand(b);
  }

  public NodeItem expand(final NodeItem nodeItem) {
    return new NodeItem(
        Math.min(nodeItem.minX, minX),
        Math.min(nodeItem.minY, minY),
        Math.max(nodeItem.maxX, maxX),
        Math.max(nodeItem.maxY, maxY),
        offset);
  }

  public boolean intersects(NodeItem nodeItem) {
    if (nodeItem.minX > maxX) {
      return false;
    }
    if (nodeItem.minY > maxY) {
      return false;
    }
    if (nodeItem.maxX < minX) {
      return false;
    }
    if (nodeItem.maxY < minY) {
      return false;
    }
    return true;
  }

  public Envelope toEnvelope() {
    return new Envelope(minX, maxX, minY, maxY);
  }
}
