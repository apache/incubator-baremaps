package io.gazetteer.helidon;

import io.gazetteer.postgis.PostgisConfig;
import io.gazetteer.postgis.PostgisLayer;
import io.gazetteer.postgis.PostgisTileSource;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.StaticContentSupport;
import io.helidon.webserver.WebServer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@Command(description = "Start a tile server")
public class TileServer implements Runnable {

    @Parameters(index = "0", paramLabel = "CONFIG_FILE", description = "The YAML configuration file.")
    private File file;

    @Override
    public void run() {
        try {
            List<PostgisLayer> layers = PostgisConfig.load(new FileInputStream(file)).getLayers();
            PostgisTileSource tileSource = new PostgisTileSource(layers);
            ServerConfiguration serverConfig = ServerConfiguration.builder()
                    .port(8081).build();
            Routing routing = Routing.builder()
                    .register("/maps", StaticContentSupport.builder("/www", this.getClass().getClassLoader())
                            .welcomeFileName("index.html")
                            .build())
                    .register(new TileService(tileSource))
                    .build();
            WebServer.create(serverConfig, routing)
                    .start()
                    .thenAccept(ws -> System.out.println("TileServer started at: http://localhost:" + ws.port()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CommandLine.run(new TileServer(), args);
    }
}
