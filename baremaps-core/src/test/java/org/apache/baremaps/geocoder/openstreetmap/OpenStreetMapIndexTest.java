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

package org.apache.baremaps.geocoder.openstreetmap;

import static org.apache.baremaps.testing.TestFiles.SAMPLE_OSM_PBF;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.baremaps.data.util.FileUtils;
import org.apache.baremaps.tasks.CreateGeocoderOpenStreetMap;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.document.ShapeField;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldExistsQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


@Disabled("prototype implementation")
public class OpenStreetMapIndexTest {

  private static Path directory;
  private static IndexSearcher searcher;

  @BeforeAll
  public static void beforeAll() throws Exception {
    // Init the geocoder service
    directory = Files.createTempDirectory(Paths.get("."), "geocoder_");

    // Create the geonames index

    var task = new CreateGeocoderOpenStreetMap(SAMPLE_OSM_PBF, directory);
    task.execute(new WorkflowContext());
    var dir = FSDirectory.open(directory);
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
        new OpenStreetMapQuery("vaduz").build();
    var topDocs = searcher.search(query, 1);
    var doc = searcher.doc(Arrays.stream(topDocs.scoreDocs).findFirst().get().doc);
    assertEquals("Vaduz", doc.getField("name").stringValue());
    System.out.println(doc);
  }


  /**
   * Querying document which contains a point with lat/long
   */
  @Test
  void testGeoQuery() throws Exception {
    var vaduzLatLong = new double[] {47.1392862, 9.5227962};
    var query = LatLonShape.newPointQuery("polygon", ShapeField.QueryRelation.CONTAINS,
        vaduzLatLong);
    var topDocs = searcher.search(query, 10);
    List<Document> docs = new ArrayList<>();
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      docs.add(searcher.doc(scoreDoc.doc));
    }
    // Vaduz OSM relation:1155956 is present in results
    // https://www.openstreetmap.org/relation/1155956
    var vaduz = docs.stream()
        .filter(doc -> Long.parseLong(doc.getField("osm_id").stringValue()) == 1155956).findFirst();
    assertTrue(vaduz.isPresent());
    assertEquals("1155956", vaduz.get().get("osm_id"));

    docs.forEach(System.out::println);
  }

  /**
   * Querying document within a polygon shape drawn as a bounding box around Vaduz
   */
  @Test
  void testPolygonQuery() throws Exception {
    // Drawing box at https://geojson.io/#map=14.18/47.13807/9.5242
    var bbox = """
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "properties": {},
              "geometry": {
                "coordinates": [
                  [
                    [
                      9.503676237856723,
                      47.14708630518214
                    ],
                    [
                      9.503676237856723,
                      47.1290480684377
                    ],
                    [
                      9.544726212280978,
                      47.1290480684377
                    ],
                    [
                      9.544726212280978,
                      47.14708630518214
                    ],
                    [
                      9.503676237856723,
                      47.14708630518214
                    ]
                  ]
                ],
                "type": "Polygon"
              }
            }
          ]
        }
        """;
    var polygon = Polygon.fromGeoJSON(bbox);
    var query = LatLonShape.newPolygonQuery("polygon", ShapeField.QueryRelation.WITHIN, polygon);
    var booleanQuery = new BooleanQuery.Builder();
    booleanQuery.add(query, BooleanClause.Occur.MUST);
    // Filter to only include document which have a name field
    booleanQuery.add(new FieldExistsQuery("name"), BooleanClause.Occur.MUST);
    var topDocs = searcher.search(booleanQuery.build(), 100);
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      System.out.println(searcher.doc(scoreDoc.doc));
    }
    assertNotNull(topDocs);
  }

}
