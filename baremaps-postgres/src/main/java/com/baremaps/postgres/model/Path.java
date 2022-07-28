// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.model;

import java.util.List;

public class Path {

    private final boolean isClosed;
    private final List<Point> points;

    public Path(boolean closed, List<Point> points) {

        if(points == null) {
            throw new IllegalArgumentException("points");
        }

        this.isClosed = closed;
        this.points = points;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public List<Point> getPoints() {
        return points;
    }

    public int size() {
        return points.size();
    }
}
