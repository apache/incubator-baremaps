package io.gazetteer.osm.data;

import io.gazetteer.common.postgis.GeometryUtils;
import java.nio.ByteBuffer;
import org.locationtech.jts.geom.Coordinate;

public class CoordinateMapper implements FixedSizeObjectMapper<Coordinate> {

  private static final int PRECISION = 7;
  private static final int MULTIPLICATION_FACTOR = calculateMultiplicationFactor();


  /**
   * Generates the multiplication factor that the double coordinate must be multiplied by to turn it into a fixed precision integer.
   *
   * @return The double to fixed multiplication factor.
   */
  private static int calculateMultiplicationFactor() {
    int result;
    result = 1;
    for (int i = 0; i < PRECISION; i++) {
      result *= 10;
    }
    return result;
  }


  /**
   * Converts the requested coordinate from double to fixed precision.
   *
   * @param coordinate The double coordinate value.
   * @return The fixed coordinate value.
   */
  public static int convertToFixed(double coordinate) {
    int result;
    result = (int) Math.round(coordinate * MULTIPLICATION_FACTOR);
    return result;
  }


  /**
   * Converts the requested coordinate from fixed to double precision.
   *
   * @param coordinate The fixed coordinate value.
   * @return The double coordinate value.
   */
  public static double convertToDouble(int coordinate) {
    double result;
    result = ((double) coordinate) / MULTIPLICATION_FACTOR;
    return result;
  }

  @Override
  public int size() {
    return 9;
  }

  @Override
  public Coordinate read(ByteBuffer buffer) {
    if (buffer.get() == 0) {
      return null;
    }
    double lon = convertToDouble(buffer.getInt());
    double lat = convertToDouble(buffer.getInt());
    return GeometryUtils.toCoordinate(lon, lat);
    //return new Coordinate(lon, lat);
  }

  @Override
  public void write(ByteBuffer buffer, Coordinate value) {
    if (value != null) {
      buffer.put((byte) 1);
      buffer.putInt(convertToFixed(value.getX()));
      buffer.putInt(convertToFixed(value.getY()));
    }
  }
}
