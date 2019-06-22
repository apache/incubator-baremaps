import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

import io.gazetteer.tileserver.TileServer;
import io.gazetteer.tilestore.Tile;
import io.gazetteer.tilestore.TileException;
import io.gazetteer.tilestore.TileReader;
import io.gazetteer.tilestore.XYZ;
import io.gazetteer.tilestore.postgis.PostgisConfig;
import io.gazetteer.tilestore.postgis.PostgisLayer;
import io.gazetteer.tilestore.postgis.PostgisTileReader;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.yaml.snakeyaml.error.YAMLException;
import picocli.CommandLine;

@CommandLine.Command(description = "Start a selenium tile server")
public class SeleniumTileServer implements Runnable {

  @CommandLine.Parameters(index = "0", paramLabel = "POSTGRES_DATABASE", description = "The Postgres database.")
  private String database;

  @CommandLine.Parameters(index = "1", paramLabel = "CONFIG_FILE", description = "The YAML configuration config.")
  private Path config;

  private EventLoopGroup bossGroup;

  private EventLoopGroup workerGroup;

  private Channel channel;

  private String url = "http://localhost:8081/";

  @Override
  public void run() {
    try {
      start();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    WebDriver driver = new ChromeDriver();
    driver.manage().window().maximize();
    driver.get(url);

    try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
      config.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
      while (true) {
        final WatchKey wk = watchService.take();
        for (WatchEvent<?> event : wk.pollEvents()) {
          final Path changed = (Path) event.context();
          if (config.getFileName().equals(changed.getFileName())) {
            try {
              stop();
              start();
              driver.get(url);
            } catch (YAMLException e) {
              e.printStackTrace();
            } catch (FileNotFoundException e) {
              e.printStackTrace();
            }
          }
        }
        wk.reset();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void start() throws FileNotFoundException {
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
            .putHeader(CONTENT_TYPE, TileServer.TILE_MIME_TYPE)
            .putHeader(CONTENT_ENCODING, TileServer.TILE_ENCODING)
            .end(Buffer.buffer(tile.getBytes()));
      } catch (TileException e) {
        routingContext.response().setStatusCode(404);
      }
    });
    router.route("/*").handler(StaticHandler.create());

    // Start the Vertx server
    server.requestHandler(router).listen(8081);
  }

  public void stop() {
    channel.close();
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
  }

  public static void main(String[] args) {
    CommandLine.run(new SeleniumTileServer(), args);
  }
}
