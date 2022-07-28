// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.model;

public class LineSegment {

    private final Point p1;
    private final Point p2;

    public LineSegment(Point p1, Point p2) {

        if(p1 == null) {
            throw new IllegalArgumentException("p1");
        }

        if(p2 == null) {
            throw new IllegalArgumentException("p2");
        }

        this.p1 = p1;
        this.p2 = p2;
    }

    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

}
