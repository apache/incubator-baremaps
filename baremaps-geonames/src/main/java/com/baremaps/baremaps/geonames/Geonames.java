package com.baremaps.baremaps.geonames;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Geonames {
 public static Stream<GeonamesRecord> parse(InputStream inputStream) throws IOException {
   return StreamSupport.stream(new GeonamesSpliterator(inputStream), false);
 }
}
