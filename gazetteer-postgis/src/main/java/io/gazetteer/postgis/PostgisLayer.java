package io.gazetteer.postgis;

public class PostgisLayer {

    private String name;

    private int minZoom;

    private int maxZoom;

    private String sql;

    public String getName() {
        return name;
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
