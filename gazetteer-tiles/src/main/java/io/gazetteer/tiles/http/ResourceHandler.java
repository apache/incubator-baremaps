package io.gazetteer.tiles.http;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;

import com.google.common.io.Resources;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class ResourceHandler implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      String path = exchange.getRequestURI().getPath();
      if (path.endsWith("/")) {
        path = String.format("%sindex.html", path);
      }
      URL resource = Resources.getResource(path.substring(1));
      byte[] bytes = Resources.toByteArray(resource);
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
