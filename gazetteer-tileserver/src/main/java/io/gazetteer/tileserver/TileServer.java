package io.gazetteer.tileserver;

import static io.vertx.core.http.HttpHeaders.CONTENT_ENCODING;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import io.gazetteer.tilestore.Tile;
import io.gazetteer.tilestore.TileException;
import io.gazetteer.tilestore.TileReader;
import io.gazetteer.tilestore.XYZ;
import io.gazetteer.tilestore.postgis.PostgisConfig;
import io.gazetteer.tilestore.postgis.PostgisLayer;
import io.gazetteer.tilestore.postgis.PostgisTileReader;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(description = "Start a tile server")
public class TileServer implements Runnable {

  public static final String TILE_ENCODING = "gzip";

  public static final String TILE_MIME_TYPE = "application/vnd.mapbox-vector-tile";

  @Parameters(index = "0", paramLabel = "CONFIG_FILE", description = "The YAML configuration config.")
  private Path config;

  @Parameters(index = "1", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
  private String database;

  @Override
  public void run() {
    try {
      // Initialize Vertx
      Vertx vertx = Vertx.vertx();
      HttpServer server = vertx.createHttpServer();

      // Read the configuration file
      List<PostgisLayer> layers = PostgisConfig.load(new FileInputStream(config.toFile())).getLayers();
      TileReader tileReader = new PostgisTileReader(database, layers);

      // Create the Vertx router
      Router router = Router.router(vertx);
      router.get("/:z/:x/:y.pbf").blockingHandler(routingContext -> {
        try {
          Integer z = Integer.parseInt(routingContext.request().getParam("z"));
          Integer x = Integer.parseInt(routingContext.request().getParam("x"));
          Integer y = Integer.parseInt(routingContext.request().getParam("y"));
          XYZ xyz = new XYZ(x, y, z);
          Tile tile = tileReader.read(xyz);
          routingContext.response()
              .putHeader(CONTENT_TYPE, TILE_MIME_TYPE)
              .putHeader(CONTENT_ENCODING, TILE_ENCODING)
              .end(Buffer.buffer(tile.getBytes()));
        } catch (TileException e) {
          routingContext.response().setStatusCode(404);
        }
      });
      router.route("/*").handler(StaticHandler.create());

      // Start the Vertx server
      server.requestHandler(router).listen(8081);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
    CommandLine.run(new TileServer(), args);
  }
}
