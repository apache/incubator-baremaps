package com.baremaps.core.data;

import com.baremaps.core.blob.BlobStore;
import com.baremaps.core.blob.BlobStoreException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Manager {

  private static final Logger logger = LoggerFactory.getLogger(Manager.class);

  private final BlobStore blobStore;

  private final Config config;

  private final Path root;

  public Manager(BlobStore blobStore, Config config, Path directory) {
    this.blobStore = blobStore;
    this.config = config;
    this.root = directory;
  }

  public void handle() {
    config.getSources().stream().parallel().forEach(this::handle);
  }

  private void handle(Source source) {
    URI uri = URI.create(source.getUrl());
    Path directory = root.resolve(source.getId());
    Path file = directory.resolve(source.getFile());

    // Download and save the source
    switch (source.getArchive()) {
      case "zip":
        downloadZip(uri, directory);
        break;
      default:
        downloadRaw(uri, directory);
        break;
    }

    // Import the source in the database
    switch (source.getFormat()) {
      case "pbf":
        // TODO: importPbf(file);
        break;
      case "shp":
        // TODO: importShp(file);
        break;
      case "sqlite":
        // TODO: importSqlite(file);
        break;
      default:
        break;
    }

    // Simplify the source at multiple zoom levels

    // Index the source

  }

  private void downloadZip(URI uri, Path path) {
    try (ZipInputStream zis = new ZipInputStream(blobStore.get(uri).getInputStream())) {
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        Path file = path.resolve(ze.getName());
        Files.createDirectories(file.getParent());
        Files.copy(zis, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException exception) {
      exception.printStackTrace();
    } catch (BlobStoreException e) {
      e.printStackTrace();
    }
  }

  private void downloadRaw(URI uri, Path path) {
    Path file = path.resolve(Paths.get(uri.getPath()).getFileName());
    try (InputStream is = blobStore.get(uri).getInputStream()) {
      Files.createDirectories(file.getParent());
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException exception) {
      exception.printStackTrace();
    } catch (BlobStoreException e) {
      e.printStackTrace();
    }
  }

}
