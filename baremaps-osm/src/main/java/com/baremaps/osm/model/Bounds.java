package com.baremaps.osm.model;

import com.baremaps.osm.EntityHandler;

public class Bounds implements Entity {

  private final double maxLat;

  private final double maxLon;

  private final double minLat;

  private final double minLon;

  public Bounds(double maxLat, double maxLon, double minLat, double minLon) {
    this.maxLat = maxLat;
    this.maxLon = maxLon;
    this.minLat = minLat;
    this.minLon = minLon;
  }

  public double getMaxLat() {
    return maxLat;
  }

  public double getMaxLon() {
    return maxLon;
  }

  public double getMinLat() {
    return minLat;
  }

  public double getMinLon() {
    return minLon;
  }

  @Override
  public void visit(EntityHandler visitor) throws Exception {
    visitor.handle(this);
  }

}
