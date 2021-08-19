package com.baremaps.postgres.jdbi;

import java.util.Map;
import java.util.StringJoiner;
import org.jdbi.v3.postgres.HStore;
import org.locationtech.jts.geom.Geometry;

public class Feature {

  private String id;

  private String type;

  private Geometry geometry;

  @HStore
  private Map<String, String> properties;

  public Feature() {

  }

  public Feature(String id, String type, Geometry geometry,
      Map<String, String> properties) {
    this.id = id;
    this.type = type;
    this.geometry = geometry;
    this.properties = properties;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setGeometry(Geometry geometry) {
    this.geometry = geometry;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Feature.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("type='" + type + "'")
        .add("geometry=" + geometry)
        .add("properties=" + properties)
        .toString();
  }
}
