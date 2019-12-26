package io.gazetteer.tiles.postgis;

public class PostgisLayer {

  private String name;

  private String geometry;

  private int minZoom;

  private int maxZoom;

  private String sql;

  public PostgisLayer() {

  }

  public PostgisLayer(String name, String geometry, int minZoom, int maxZoom, String sql) {
    this.name = name;
    this.geometry = geometry;
    this.minZoom = minZoom;
    this.maxZoom = maxZoom;
    this.sql = sql;
  }

  public String getName() {
    return name;
  }

  public String getGeometry() {
    return geometry;
  }

  public int getMinZoom() {
    return minZoom;
  }

  public int getMaxZoom() {
    return maxZoom;
  }

  public String getSql() {
    return sql;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setGeometry(String geometry) {
    this.geometry = geometry;
  }

  public void setMinZoom(int minZoom) {
    this.minZoom = minZoom;
  }

  public void setMaxZoom(int maxZoom) {
    this.maxZoom = maxZoom;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }
}
