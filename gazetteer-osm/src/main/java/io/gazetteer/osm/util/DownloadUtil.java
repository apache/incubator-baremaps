package io.gazetteer.osm.util;

import static com.google.common.base.Preconditions.checkArgument;

import io.gazetteer.osm.model.State;

public class DownloadUtil {

  public static void next(State state) {
    long sequenceNumber = state.sequenceNumber + 1;
    System.out.println(sequenceNumber);
  }

  public static String path(long sequenceNumber) {
    checkArgument(sequenceNumber <= 999999999);
    String leading = String.format("%09d", sequenceNumber);
    return leading.substring(0, 3) + "/"
        + leading.substring(3, 6) + "/"
        + leading.substring(6, 9);
  }

  public static String osc(long sequenceNumber) {
    return path(sequenceNumber) + ".osc.gz";
  }

  public static String state(long sequenceNumber) {
    return path(sequenceNumber) + ".state.txt";
  }

}
