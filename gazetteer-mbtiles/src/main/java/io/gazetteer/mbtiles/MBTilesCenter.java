package io.gazetteer.mbtiles;

public class MBTilesCenter {

    public final double longitude, latitude;

    public final int zoom;

    public MBTilesCenter(double longitude, double latitude, int zoom) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.zoom = zoom;
    }

    @Override
    public String toString() {
        return String.format("%f,%f,%d", longitude, latitude, zoom);
    }

    public static MBTilesCenter fromString(String center) {
        if (center == null) return null;
        String[] arr = center.split(",");
        if (arr.length != 4) return null;
        try {
            double longitude = Double.parseDouble(arr[0]);
            double latitude = Double.parseDouble(arr[1]);
            int zoom = Integer.parseInt(arr[2]);
            return new MBTilesCenter(longitude, latitude, zoom);
        } catch (Exception e) {
            return null;
        }
    }
}
