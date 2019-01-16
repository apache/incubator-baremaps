package io.gazetteer.mbtiles;

import com.google.common.base.Objects;

public class Center {

    public final double longitude, latitude;

    public final int zoom;

    public Center(double longitude, double latitude, int zoom) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.zoom = zoom;
    }

    public String serialize() {
        return String.format("%f,%f,%d", longitude, latitude, zoom);
    }

    public static Center deserialize(String center) {
        if (center == null) return null;
        String[] arr = center.split(",");
        if (arr.length != 4) return null;
        try {
            double longitude = Double.parseDouble(arr[0]);
            double latitude = Double.parseDouble(arr[1]);
            int zoom = Integer.parseInt(arr[2]);
            return new Center(longitude, latitude, zoom);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Center center = (Center) o;
        return Double.compare(center.longitude, longitude) == 0 &&
                Double.compare(center.latitude, latitude) == 0 &&
                zoom == center.zoom;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(longitude, latitude, zoom);
    }
}
