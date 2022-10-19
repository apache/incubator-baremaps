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



import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;

public abstract class Geocoder implements AutoCloseable {

  private final Directory directory;
  private SearcherManager searcherManager;
  private Analyzer analyzer = analyzer();

  public Geocoder(Path index) throws IOException {
    this.directory = MMapDirectory.open(index);
  }

  public boolean indexExists() throws IOException {
    return DirectoryReader.indexExists(directory);
  }

  public void open() throws IOException {
    if (!DirectoryReader.indexExists(directory)) {
      throw new IllegalStateException("Invalid Lucene index directory");
    }
    searcherManager = new SearcherManager(directory, new SearcherFactory());
  }

  public void build() throws IOException {
    build(documents()::iterator);
  }

  private void build(Iterable<Document> documents) throws IOException {
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    try (IndexWriter indexWriter = new IndexWriter(directory, config)) {
      indexWriter.deleteAll();
      indexWriter.addDocuments(documents);
    } catch (IOException exception) {
      throw new RuntimeException();
    }
    searcherManager = new SearcherManager(directory, new SearcherFactory());
  }

  public Response search(Request request) throws IOException, ParseException {
    IndexSearcher searcher = searcherManager.acquire();
    List<Result> results = new ArrayList<>();
    try {
      TopDocs topDocs = searcher.search(query(analyzer, request), request.limit());
      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        results.add(new Result(scoreDoc, searcher.doc(scoreDoc.doc)));
      }
      return new Response(topDocs, results);
    } finally {
      searcherManager.release(searcher);
    }
  }

  @Override
  public void close() throws IOException {
    analyzer.close();
    directory.close();
    searcherManager.close();
  }

  protected abstract Analyzer analyzer() throws IOException;

  protected abstract Stream<Document> documents() throws IOException;

  protected abstract Query query(Analyzer analyzer, Request request) throws ParseException;
}
