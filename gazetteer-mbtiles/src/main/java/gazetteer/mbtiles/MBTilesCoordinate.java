package gazetteer.mbtiles;

public class MBTilesCoordinate {

    public final int z, x, y;

    public MBTilesCoordinate(int z, int x, int y) {
        this.z = z;
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MBTilesCoordinate that = (MBTilesCoordinate) o;
        if (z != that.z) return false;
        if (x != that.x) return false;
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = z;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }
}