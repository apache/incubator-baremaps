package com.baremaps.cli;

import com.baremaps.geocoder.GeocoderLucene;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "geocoder-geonames-search", description = "Transform a text into a geocode point.")
public class GeocoderGeonamesSearch implements Callable<Integer> {

  @Option(
      names = {"--index"},
      paramLabel = "INDEX",
      description = "The path to the lucene index.",
      required = true)
  private Path indexPath;

  @Option(
      names = {"--search-value"},
      paramLabel = "SEARCH_VALUE",
      description = "The address to find in the index.",
      required = true)
  private String searchValue;

  @Override
  public Integer call() throws Exception {
    GeocoderLucene geocoderLucene = new GeocoderLucene(indexPath);
    geocoderLucene.search(searchValue);

    return 0;
  }
}
