package com.baremaps.cli.geocoder;

import com.baremaps.grpc.server.GeocoderServer;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

@Command(name = "start-grpc", description = "Run grpc geocoder service.")
public class StartGrpc implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    GeocoderServer.run();
    return 0;
  }
}
