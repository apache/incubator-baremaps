package com.baremaps.cli.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import com.linecorp.armeria.common.sse.ServerSentEvent;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class ChangePublisher implements Publisher<ServerSentEvent> {

  private static Logger logger = LogManager.getLogger();

  private final List<Subscriber<? super ServerSentEvent>> subscribers = new CopyOnWriteArrayList<>();

  public ChangePublisher(Path directory) {
    new Thread(() -> {
      try {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            dir.register(watchService, ENTRY_MODIFY);
            return FileVisitResult.CONTINUE;
          }
        });
        WatchKey key;
        while ((key = watchService.take()) != null) {
          Path dir = (Path) key.watchable();
          for (WatchEvent<?> event : key.pollEvents()) {
            Path path = dir.resolve((Path) event.context());
            for (Subscriber<? super ServerSentEvent> subscriber : subscribers) {
              subscriber.onNext(ServerSentEvent.ofData(directory.relativize(path).toString()));
              subscriber.onComplete();
            }
          }
          key.reset();
        }
      } catch (InterruptedException e) {
        logger.error(e);
      } catch (IOException e) {
        logger.error(e);
      }
    }).start();
  }

  @Override
  public void subscribe(Subscriber<? super ServerSentEvent> subscriber) {
    subscribers.add(subscriber);
  }

}
