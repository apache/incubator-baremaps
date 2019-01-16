package io.gazetteer.tileserver.postgis;

public class PGLayer {

    private final String name;

    private final String geometry;

    private final String database;

    private final String sql;

    private final int minZoom;

    private final int maxZoom;

    public PGLayer(String name, String geometry, String database, String sql, int minZoom, int maxZoom) {
        this.name = name;
        this.geometry = geometry;
        this.database = database;
        this.sql = sql;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
    }

    public String getName() {
        return name;
    }

    public String getGeometry() {
        return geometry;
    }

    public String getDatabase() {
        return database;
    }

    public String getSql() {
        return sql;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

}
