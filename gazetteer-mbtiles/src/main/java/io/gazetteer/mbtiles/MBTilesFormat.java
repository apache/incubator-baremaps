package io.gazetteer.mbtiles;

public enum MBTilesFormat {
    pbf("application/vnd.mapbox-vector-tile"),
    png("image/png"),
    jpg("image/jpeg"),
    json("text/json");

    public String mimeType;

    MBTilesFormat(String mime) {
        this.mimeType = mime;
    }
}