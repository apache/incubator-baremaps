package io.gazetteer.cli.serve;

import com.google.common.io.Resources;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URL;

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
      exchange.sendResponseHeaders(200, bytes.length);
      exchange.getResponseBody().write(bytes);
    } finally {
      exchange.close();
    }
  }
}
