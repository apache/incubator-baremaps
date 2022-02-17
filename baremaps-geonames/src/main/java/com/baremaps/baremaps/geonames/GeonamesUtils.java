package com.baremaps.baremaps.geonames;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GeonamesUtils {
 public static Stream<GeonamesRecord> parse(InputStream inputStream) throws IOException {
   return StreamSupport.stream(new GeonamesSpliterator(inputStream), false);
 }
}
