/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.geocoder;

import com.baremaps.baremaps.geonames.GeonamesRecord;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/** Represent geonames geocoder */
public class GeonamesGeocoder {
  private Path indexPath;

  /**
   * Constructs a {@link GeonamesGeocoder}
   *
   * @param indexPath - the path to the lucene index
   * @throws IOException
   */
  public GeonamesGeocoder(Path indexPath) throws IOException {
    this.indexPath = indexPath;
  }

  /**
   * Index {@link GeonamesRecord} in a lucene index
   *
   * @param geonamesRecords - Stream of {@link GeonamesRecord}
   * @throws IOException
   */
  public void indexGeonames(Stream<GeonamesRecord> geonamesRecords) throws IOException {
    Directory directory = FSDirectory.open(indexPath);

    try (Analyzer analyzer = new StandardAnalyzer()) {
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      try (IndexWriter indexWriter = new IndexWriter(directory, config)) {
        geonamesRecords.forEach(
            geonamesRecord ->
                index(
                    indexWriter,
                    geonamesRecord.name,
                    geonamesRecord.countryCode,
                    geonamesRecord.longitude,
                    geonamesRecord.latitude));
      }
    }
    directory.close();
  }

  private void index(
      IndexWriter indexWriter, String name, String countryCode, Double longitude, Double latitude) {
    try {
      Document doc = new Document();
      doc.add(new Field("name", name, TextField.TYPE_STORED));
      doc.add(new Field("countryCode", countryCode, StringField.TYPE_STORED));
      doc.add(new StoredField("longitude", longitude));
      doc.add(new StoredField("latitude", latitude));
      indexWriter.addDocument(doc);
    } catch (Exception e) {
      throw new RuntimeException(
          "An error occurred while creating Lucene document for Geonames data");
    }
  }

  /**
   * Search into geonames lucene index and show the best 10 results
   *
   * @param searchValue - the value to be search in the index
   * @throws IOException
   * @throws ParseException
   */
  public void search(String searchValue) throws IOException, ParseException {
    Directory directory = FSDirectory.open(indexPath);
    DirectoryReader indexReader = DirectoryReader.open(directory);
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    BooleanQuery.Builder booleanQueryBuilder = new Builder();

    try (StandardAnalyzer analyzer = new StandardAnalyzer()) {
      Query q1 = new QueryParser("name", analyzer).parse(searchValue);
      Query q2 = new QueryParser("countryCode", analyzer).parse(searchValue);

      booleanQueryBuilder.add(q1, Occur.SHOULD);
      booleanQueryBuilder.add(q2, Occur.SHOULD);
    }

    ScoreDoc[] hits = indexSearcher.search(booleanQueryBuilder.build(), 10).scoreDocs;

    for (int i = 0; i < hits.length; ++i) {
      int docId = hits[i].doc;
      Document d = indexSearcher.doc(docId);
      System.out.println(
          (i + 1)
              + ". "
              + d.get("name")
              + "\t"
              + d.get("countryCode")
              + "\t("
              + d.get("longitude")
              + ":"
              + d.get("latitude")
              + ")");
    }

    indexReader.close();
    directory.close();
  }
}
