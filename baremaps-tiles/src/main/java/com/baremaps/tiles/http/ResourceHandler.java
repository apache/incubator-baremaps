package com.baremaps.tiles.http;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceHandler implements HttpHandler {

  private static final Logger logger = LoggerFactory.getLogger(ResourceHandler.class);

  private final Path directory;

  public ResourceHandler(Path directory) {
    this.directory = directory;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      String path = exchange.getRequestURI().getPath();
      logger.info("GET {}", path);

      if (path.endsWith("/")) {
        path = String.format("%sindex.html", path);
      }

      Path file = directory.resolve(path.substring(1));
      byte[] bytes = Files.readAllBytes(file);
      exchange.getResponseHeaders().put(ACCESS_CONTROL_ALLOW_ORIGIN, Arrays.asList("*"));
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      exchange.close();
    }
  }
}
