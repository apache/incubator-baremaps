package io.gazetteer.tilestore.util;

import io.gazetteer.tilestore.XYZ;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;

public class TileUtil {

  public static List<XYZ> overlappingXYZ(Envelope envelope, int minZ, int maxZ) {
    ArrayList<XYZ> coordinates = new ArrayList<>();
    for (int z = minZ; z <= maxZ; z++) {
      XYZ min = xyz(envelope.getMinX(), envelope.getMaxY(), z);
      XYZ max = xyz(envelope.getMaxX(), envelope.getMinY(), z);
      for (int x = min.getX(); x <= max.getX(); x++) {
        for (int y = min.getY(); y <= max.getY(); y++) {
          XYZ xyz = new XYZ(x, y, z);
          coordinates.add(xyz);
        }
      }
    }
    return coordinates;
  }

  public static XYZ xyz(double lon, double lat, int z) {
    int x = (int) ((lon + 180.0) / 360.0 * (1 << z));
    int y = (int) ((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0 * (1 << z));
    return new XYZ(x, y, z);
  }

  public static void main(String[] args) {
    GeometryFactory factory = new GeometryFactory();
    List<XYZ> coordinates = overlappingXYZ(new Envelope(1,2,1,2), 12, 14);
    for (XYZ c : coordinates) {
      System.out.println(c);
    }
  }

}
