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

package org.apache.baremaps.geocoder;

import static org.apache.baremaps.testing.TestFiles.DATA_OSM_PBF;
import static org.apache.baremaps.testing.TestFiles.LIECHTENSTEIN_OSM_PBF;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.baremaps.geocoderosm.GeocoderOSMQuery;
import org.apache.baremaps.testing.TestFiles;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.tasks.CreateGeocoderOpenStreetMap;
import org.apache.baremaps.workflow.tasks.CreateGeonamesIndex;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class OSMIndexTest {

  private static Path directory;
  private static IndexSearcher searcher;

  @BeforeAll
  public static void beforeAll() throws Exception {
    // Init the geocoder service
    directory = Files.createTempDirectory(Paths.get("."), "geocoder_");

    // Create the geonames index

    var task = new CreateGeocoderOpenStreetMap(LIECHTENSTEIN_OSM_PBF, 3857, directory);
    task.execute(new WorkflowContext());
    var dir = MMapDirectory.open(directory);
    var searcherManager = new SearcherManager(dir, new SearcherFactory());
    searcher = searcherManager.acquire();
  }

  @AfterAll
  public static void afterAll() throws IOException {
    FileUtils.deleteRecursively(directory);
  }

  @Test
  void testCreateIndex() throws Exception {
    var query =
        new GeocoderOSMQuery("vaduz").build();
    var topDocs = searcher.search(query, 1);
    var doc = searcher.doc(Arrays.stream(topDocs.scoreDocs).findFirst().get().doc);
    assertEquals("Vaduz", doc.getField("name").stringValue());
    System.out.println(doc);
  }

}
