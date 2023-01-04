package org.apache.baremaps.tdtiles.building;

import org.locationtech.jts.geom.Geometry;

public record Building(Geometry geometry, float height) {

}
