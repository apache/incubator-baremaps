/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.server;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatcher implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(DevResources.class);

  private final Set<Path> directories;

  private final Consumer<Path> consumer;

  public DirectoryWatcher(Set<Path> directories, Consumer<Path> consumer) {
    this.directories = directories;
    this.consumer = consumer;
  }

  @Override
  public void run() {
    try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
      for (Path directory : directories) {
        directory.register(watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
      }
      WatchKey key;
      while ((key = watchService.take()) != null) {
        Path dir = (Path) key.watchable();
        for (WatchEvent<?> event : key.pollEvents()) {
          Path path = dir.resolve((Path) event.context()).toAbsolutePath();
          logger.info("Change detected in {}", path);
          consumer.accept(path);
        }
        key.reset();
      }
    } catch (InterruptedException | IOException e) {
      logger.error(e.getMessage());
      Thread.currentThread().interrupt();
    }
  }
}
