package com.baremaps.grpc.server;

import com.baremaps.geocoder.Geocoder;
import com.baremaps.geocoder.geonames.GeonamesGeocoder;
import com.baremaps.grpc.service.GeocoderServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeocoderServer {
  private static final Logger logger = LoggerFactory.getLogger(GeocoderServer.class);

  private Server server;

  private void start() throws IOException, URISyntaxException {
    /* The port on which the server should run */
    int port = 50051;
    logger.info("Get geonames data.");
    Path indexPath = Paths.get("geonamesIndex");
    URI geonamesData = getClass().getClassLoader().getResource("geonames_sample.txt").toURI();
    Geocoder geocoder = new GeonamesGeocoder(indexPath, geonamesData);
    logger.info("Index Geocoder.");
    geocoder.build();
    logger.info("Index finished.");
    logger.info("Start grpc server.");
    server = ServerBuilder.forPort(port)
        .addService(new GeocoderServiceImpl(geocoder))
        .build()
        .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        try {
          GeocoderServer.this.stop();
        } catch (InterruptedException e) {
          e.printStackTrace(System.err);
        }
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
    final GeocoderServer server = new GeocoderServer();
    server.start();
    server.blockUntilShutdown();
  }
}
