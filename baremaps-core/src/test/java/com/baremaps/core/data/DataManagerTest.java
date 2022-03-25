package com.baremaps.core.data;

import com.baremaps.collection.utils.FileUtils;
import com.baremaps.core.blob.BlobStore;
import com.baremaps.core.blob.BlobStoreRouter;
import com.baremaps.core.database.PostgresBaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataManagerTest extends PostgresBaseTest {

  Path sources;

  DataSource dataSource;

  DataManager dataManager;

  @BeforeEach
  public void before() throws IOException, SQLException {
    BlobStore blobStore = new BlobStoreRouter();
    Config config = new ObjectMapper().readValue(Resources.getResource("data.json"), Config.class);
    sources = Files.createDirectories(Paths.get("sources"));
    dataSource = initDataSource();
    dataManager = new DataManager(blobStore, dataSource, config, sources);
  }

  @AfterEach
  public void after() throws IOException {
    FileUtils.deleteRecursively(sources);
  }

  @Test
  @Ignore
  public void download() {
    dataManager.execute();
  }



}