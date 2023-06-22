package org.apache.baremaps.geocoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.baremaps.testing.TestFiles;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.tasks.CreateGeonamesIndex;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class GeonamesIndexTest {

  private static Path directory;

  @BeforeAll
  public static void beforeAll() throws Exception {
    // Init the geocoder service
    directory = Files.createTempDirectory(Paths.get("."), "geocoder_");

    // Create the geonames index
    var data = TestFiles.resolve("geonames/LI.txt");
    var task = new CreateGeonamesIndex(data, directory);
    task.execute(new WorkflowContext());
  }
  @AfterAll
  public static void afterAll() throws IOException {
    FileUtils.deleteRecursively(directory);
  }

  @Test
  void testCreateIndex() throws Exception {
    var dir = MMapDirectory.open(directory);
    var searcherManager = new SearcherManager(dir, new SearcherFactory());
    var searcher = searcherManager.acquire();

    var geonamesQuery =
        new GeonamesQueryBuilder().queryText("vaduz").countryCode("LI").build();
    var topDocs = searcher.search(geonamesQuery, 1);
    var doc = searcher.doc(Arrays.stream(topDocs.scoreDocs).findFirst().get().doc);
    var explain = searcher.explain(geonamesQuery, Arrays.stream(topDocs.scoreDocs).findFirst().get().doc);
    var x =2;
  }
}
