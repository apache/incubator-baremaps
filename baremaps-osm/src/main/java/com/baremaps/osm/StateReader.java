package com.baremaps.osm;

import com.baremaps.osm.domain.State;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class StateReader {

  private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  private final InputStreamReader reader;

  public StateReader(InputStream inputStream) {
    this.reader = new InputStreamReader(inputStream, Charsets.UTF_8);
  }

  public State read() throws IOException {
    String state = CharStreams.toString(reader);
    Map<String, String> map = new HashMap<>();
    for (String line : state.split("\n")) {
      String[] array = line.split("=");
      if (array.length == 2) {
        map.put(array[0], array[1]);
      }
    }
    long sequenceNumber = Long.parseLong(map.get("sequenceNumber"));
    LocalDateTime timestamp = LocalDateTime.parse(map.get("timestamp"), format);
    return new State(sequenceNumber, timestamp);
  }
}
