package io.gazetteer.osm.osmxml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class State {

  public final long sequenceNumber;

  public final long timestamp;

  public State(long sequenceNumber, long timestamp) {
    this.sequenceNumber = sequenceNumber;
    this.timestamp = timestamp;
  }

  public static State parse(String state) throws ParseException {
    Map<String, String> map = new HashMap<>();
    for (String line : state.split("\n")) {
      System.out.println(line);
      String[] array = line.split("=");
      if (array.length == 2) {
        map.put(array[0], array[1]);
      }
    }
    long sequenceNumber = Long.parseLong(map.get("sequenceNumber"));
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH\\:mm\\:ss'Z'");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    long timestamp = format.parse(map.get("timestamp")).getTime() / 1000;
    return new State(sequenceNumber, timestamp);
  }

}
