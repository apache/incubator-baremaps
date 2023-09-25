/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.geocoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.baremaps.testing.TestFiles;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.tasks.CreateGeonamesIndex;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class GeonamesIndexTest {

  private static Path directory;
  private static IndexSearcher searcher;

  @BeforeAll
  public static void beforeAll() throws Exception {
    // Init the geocoder service
    directory = Files.createTempDirectory(Paths.get("."), "geocoder_");

    // Create the geonames index
    var data = TestFiles.resolve("geonames/LI.txt");
    var task = new CreateGeonamesIndex(data, directory);
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
    var geonamesQuery =
        new GeonamesQueryBuilder().queryText("vaduz").countryCode("LI").build();
    var topDocs = searcher.search(geonamesQuery, 1);
    var doc = searcher.doc(Arrays.stream(topDocs.scoreDocs).findFirst().get().doc);
    assertEquals("Vaduz", doc.getField("name").stringValue());
  }

  @Test
  void testOrQuery() throws Exception {
    var geonamesQuery =
        new GeonamesQueryBuilder().queryText("vaduz berlin").countryCode("LI").build();
    var topDocs = searcher.search(geonamesQuery, 1);
    var doc = searcher.doc(Arrays.stream(topDocs.scoreDocs).findFirst().get().doc);
    assertEquals("Vaduz", doc.getField("name").stringValue());
  }

  @Test
  void testAndQueryNoHits() throws Exception {
    var geonamesQuery =
        new GeonamesQueryBuilder().queryText("vaduz berlin").andOperator().countryCode("LI")
            .build();
    var topDocs = searcher.search(geonamesQuery, 1);
    assertEquals(0, topDocs.totalHits.value);
  }

  @Test
  void testAndQuery() throws Exception {
    var geonamesQuery =
        new GeonamesQueryBuilder().queryText("vaduz liechtenstein").andOperator()
            .countryCode("LI").build();
    var topDocs = searcher.search(geonamesQuery, 1);
    var doc = searcher.doc(Arrays.stream(topDocs.scoreDocs).findFirst().get().doc);
    assertEquals("Vaduz", doc.getField("name").stringValue());
  }
}
