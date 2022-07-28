// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.model;

import java.util.List;

public class Polygon {

    private final List<Point> points;

    public Polygon(List<Point> points) {

        if(points == null) {
            throw new IllegalArgumentException("points");
        }

        this.points = points;
    }

    public List<Point> getPoints() {
        return points;
    }

    public int size() {
        return points.size();
    }
}