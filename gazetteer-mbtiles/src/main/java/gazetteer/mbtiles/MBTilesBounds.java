package gazetteer.mbtiles;

public class MBTilesBounds {

    public final double left, bottom, right, top;

    public MBTilesBounds(double left, double bottom, double right, double top) {
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.top = top;
    }

    @Override
    public String toString() {
        return String.format("%f,%f,%f,%f", left, bottom, right, top);
    }

    public static MBTilesBounds fromString(String bounds) {
        if (bounds == null) return null;
        String[] arr = bounds.split(",");
        if (arr.length != 4) return null;
        try {
            double left = Double.parseDouble(arr[0]);
            double bottom = Double.parseDouble(arr[1]);
            double right = Double.parseDouble(arr[2]);
            double top = Double.parseDouble(arr[3]);
            return new MBTilesBounds(left, bottom, right, top);
        } catch (Exception e) {
            return null;
        }
    }

}