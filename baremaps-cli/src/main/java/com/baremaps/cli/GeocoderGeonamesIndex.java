package com.baremaps.cli;

import com.baremaps.baremaps.geonames.Geonames;
import com.baremaps.baremaps.geonames.GeonamesRecord;
import com.baremaps.geocoder.GeocoderLucene;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "geocoder-geonames-index", description = "Transform a text into a geocode point.")
public class GeocoderGeonamesIndex implements Callable<Integer> {

  @Option(
      names = {"--index"},
      paramLabel = "INDEX",
      description = "The path to the lucene index.",
      required = true)
  private Path indexPath;

  @Option(
      names = {"--geonames"},
      paramLabel = "GEONAMES",
      description = "The path to the geonames data.",
      required = true)
  private URI geonamesDataPath;

  @Override
  public Integer call() throws Exception {
    GeocoderLucene geocoderLucene = new GeocoderLucene(indexPath);
    Geonames geonames = new Geonames();


    try (InputStream inputStream = new BufferedInputStream(geonamesDataPath.toURL().openStream())) {
      Stream<GeonamesRecord> geonamesRecordStream = geonames.parse(inputStream);
      geocoderLucene.indexGeonames(geonamesRecordStream);
    }

    return 0;
  }
}
