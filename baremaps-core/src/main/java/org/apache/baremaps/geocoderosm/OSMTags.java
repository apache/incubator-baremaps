package org.apache.baremaps.geocoderosm;

public enum OSMTags {
  NAME("name"),
  PLACE("place"),
  POPULATION("population"),
  LATITUDE("latitude"),
  LONGITUDE("longitude");

  private final String key;

  OSMTags(String key) {
    this.key = key;
  }

  @Override
  public String toString() {
    return key;
  }

  public String key() {
    return key;
  }
}
