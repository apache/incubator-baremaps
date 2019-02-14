package io.gazetteer.tileserver;

import java.io.IOException;
import java.nio.file.*;

public class ChangeWatcher implements Runnable {

    private final Path config;
    private final TileServer server;

    public ChangeWatcher(Path config, TileServer server) {
        this.config = config;
        this.server = server;
    }

    @Override
    public void run() {
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            config.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                final WatchKey wk = watchService.take();
                for (WatchEvent<?> event : wk.pollEvents()) {
                    final Path changed = (Path) event.context();
                    System.out.println(changed);
                    if (changed.equals(config.getFileName())) {
                        //server.restart();
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
}
