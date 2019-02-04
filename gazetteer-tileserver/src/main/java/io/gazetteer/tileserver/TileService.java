package io.gazetteer.tileserver;

import io.gazetteer.tilesource.XYZ;
import io.gazetteer.tilesource.TileSource;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public class TileService implements Service {

  private final TileSource tileSource;

  public TileService(TileSource tileSource) {
    this.tileSource = tileSource;
  }

  @Override
  public void update(Routing.Rules rules) {
    rules.get("/{z:\\d+}/{x:\\d+}/{y:\\d+}.pbf", this::getTileHandler);
  }

  private void getTileHandler(ServerRequest request, ServerResponse response) {
    int x = Integer.parseInt(request.path().param("x"));
    int y = Integer.parseInt(request.path().param("y"));
    int z = Integer.parseInt(request.path().param("z"));
    tileSource
        .getTile(new XYZ(x, y, z))
        .thenAccept(
            tile -> {
              response.headers().add("Content-Type", tileSource.getMimeType());
              response.headers().add("Content-Length", Integer.toString(tile.getBytes().length));
              response.headers().add("Content-Encoding", "gzip");
              response.send(tile.getBytes());
            });
  }
}
