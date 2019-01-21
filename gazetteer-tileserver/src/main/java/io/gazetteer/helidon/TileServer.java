package io.gazetteer.helidon;

import io.gazetteer.postgis.PostgisConfig;
import io.gazetteer.postgis.PostgisLayer;
import io.gazetteer.postgis.PostgisTileSource;
import io.helidon.webserver.*;
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
            Service staticContent = StaticContentSupport
                    .builder("/www", this.getClass().getClassLoader())
                    .welcomeFileName("index.html")
                    .build();
            Routing routing = Routing.builder()
                    .register(staticContent)
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
