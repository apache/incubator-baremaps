package com.baremaps.blob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DownloadManager {

  private final BlobStore blobStore;

  public DownloadManager(BlobStore blobStore) {
    this.blobStore = blobStore;
  }

  public Path download(URI uri) throws BlobStoreException {
    if (uri.getScheme() == null || uri.getScheme().equals("file")) {
      return Paths.get(uri.getPath());
    } else {
      try {
        File tempFile = File.createTempFile("baremaps_", ".tmp");
        tempFile.deleteOnExit();
        try (InputStream input = blobStore.get(uri).getInputStream()) {
          Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile.toPath();
      } catch (IOException e) {
        throw new BlobStoreException(e);
      }
    }
  }

}
