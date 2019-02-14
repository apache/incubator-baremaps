import io.gazetteer.tileserver.TileServerHandler;
import io.gazetteer.tilesource.TileSource;
import io.gazetteer.tilesource.postgis.PostgisConfig;
import io.gazetteer.tilesource.postgis.PostgisLayer;
import io.gazetteer.tilesource.postgis.PostgisTileSource;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import picocli.CommandLine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@CommandLine.Command(description = "Start a selenium tile server")
public class SeleniumTileServer implements Runnable {

  @CommandLine.Parameters(index = "0", paramLabel = "CONFIG_FILE", description = "The YAML configuration path.")
  private Path path;

  private EventLoopGroup bossGroup;

  private EventLoopGroup workerGroup;

  private Channel channel;

  private String url = "http://localhost:8081/";

  @Override
  public void run() {
    start();

    WebDriver driver = new ChromeDriver();
    driver.manage().window().maximize();
    driver.get(url);

    try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
      path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
      while (true) {
        final WatchKey wk = watchService.take();
        for (WatchEvent<?> event : wk.pollEvents()) {
          final Path changed = (Path) event.context();
          if (path.getFileName().equals(changed.getFileName())) {
            System.out.println(changed.getFileName());
            stop();
            start();
            driver.get(url);
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

  private void startClient() {

  }

  public void start() {
    InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();
    try {
      List<PostgisLayer> layers =
          PostgisConfig.load(new FileInputStream(path.toFile())).getLayers();
      TileSource tileSource = new PostgisTileSource(layers);
      ServerBootstrap b = new ServerBootstrap();
      b.option(ChannelOption.SO_BACKLOG, 1024);
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws FileNotFoundException {
                  ChannelPipeline p = ch.pipeline();
                  p.addLast(new HttpServerCodec());
                  p.addLast(new HttpObjectAggregator(512 * 1024));
                  p.addLast(
                      new CorsHandler(
                          CorsConfigBuilder.forOrigin("*")
                              .allowedRequestMethods(HttpMethod.POST)
                              .build()));
                  p.addLast(new HttpServerExpectContinueHandler());
                  p.addLast(new TileServerHandler(tileSource));
                }
              });
      channel = b.bind("localhost", 8081).sync().channel();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void stop() throws InterruptedException {
    channel.close();
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
  }

  public static void main(String[] args) {
    CommandLine.run(new SeleniumTileServer(), args);
  }
}
