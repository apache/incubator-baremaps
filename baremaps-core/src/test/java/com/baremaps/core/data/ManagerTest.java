package com.baremaps.core.data;

import com.baremaps.collection.utils.FileUtils;
import com.baremaps.core.blob.BlobStore;
import com.baremaps.core.blob.BlobStoreRouter;
import com.baremaps.core.blob.HttpBlobStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ManagerTest {

  Path sources;

  Manager manager;

  @BeforeEach
  public void before() throws IOException {
    BlobStore blobStore = new BlobStoreRouter()
        .addScheme("http", new HttpBlobStore())
        .addScheme("https", new HttpBlobStore());
    Config config = new ObjectMapper().readValue(Resources.getResource("data.json"), Config.class);
    sources = Files.createDirectories(Paths.get("sources"));
    manager = new Manager(blobStore, config, sources);
  }

  @AfterEach
  public void after() throws IOException {
    FileUtils.deleteRecursively(sources);
  }

  @Test
  @Ignore
  public void download() {
    manager.handle();
  }

}