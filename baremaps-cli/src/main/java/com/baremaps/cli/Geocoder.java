package com.baremaps.cli;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "geocoder", description = "Transform a text into a geocode point.")
public class Geocoder implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    return null;
  }
}
