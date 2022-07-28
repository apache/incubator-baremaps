// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.baremaps.postgres.model;

public class Circle {

    private final Point center;
    private final double radius;

    public Circle(Point center, double radius) {
        if(center == null) {
            throw new IllegalArgumentException("center");
        }
        this.center = center;
        this.radius = radius;
    }

    public Point getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

}
