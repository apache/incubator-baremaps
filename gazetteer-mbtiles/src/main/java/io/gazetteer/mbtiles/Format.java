package io.gazetteer.mbtiles;

public enum Format {

    pbf("application/vnd.mapbox-vector-tile"),
    png("image/png"),
    jpg("image/jpeg"),
    json("text/json");

    public String mimeType;

    Format(String mime) {
        this.mimeType = mime;
    }

}