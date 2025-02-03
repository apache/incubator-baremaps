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
import org.apache.baremaps.calcite.geoparquet.GeoParquetDataTable;
import org.apache.baremaps.data.util.FileUtils;
import org.apache.baremaps.testing.TestFiles;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class DataTableIndexTest {

  private static Path directory;
  private static IndexSearcher searcher;

  @BeforeAll
  public static void beforeAll() throws Exception {
    // Init the geocoder service
    directory = Files.createTempDirectory(Paths.get("."), "geocoder_");

    // Create the geonames index
    var dir = FSDirectory.open(directory);
    var data = TestFiles.resolve("baremaps-testing/data/samples/example.parquet");
    var config = new IndexWriterConfig(GeocoderConstants.ANALYZER);
    try (var indexWriter = new IndexWriter(dir, config)) {
      indexWriter.deleteAll();
      var documents = new GeoParquetDataTable(data.toUri())
          .stream()
          .map(new DataRowMapper());
      indexWriter.addDocuments((Iterable<Document>) documents::iterator);
    }

    var searcherManager = new SearcherManager(dir, new SearcherFactory());
    searcher = searcherManager.acquire();
  }

  @AfterAll
  public static void afterAll() throws IOException {
    FileUtils.deleteRecursively(directory);
  }

  @Test
  void testQueryNoHits() throws Exception {
    var geonamesQuery = new DataTableQueryBuilder()
        .column("continent", 1.0f)
        .query("test")
        .build();
    var topDocs = searcher.search(geonamesQuery, 1);
    assertEquals(0, topDocs.totalHits.value);
  }

  @Test
  void testQuery() throws Exception {
    var geonamesQuery = new DataTableQueryBuilder()
        .column("continent", 1.0f)
        .query("oceania")
        .build();
    var topDocs = searcher.search(geonamesQuery, 1);
    var doc = searcher.doc(Arrays.stream(topDocs.scoreDocs).findFirst().get().doc);
    assertEquals("Oceania", doc.getField("continent").stringValue());
  }
}
