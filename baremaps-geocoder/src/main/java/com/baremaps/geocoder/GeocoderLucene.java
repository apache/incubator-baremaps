package com.baremaps.geocoder;

import com.baremaps.baremaps.geonames.GeonamesRecord;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class GeocoderLucene {
  private Path indexPath;

  public GeocoderLucene(Path indexPath) throws IOException {
    this.indexPath = indexPath;
  }

  public void indexGeonames(Stream<GeonamesRecord> geonamesRecords) throws IOException {
    Directory directory = FSDirectory.open(indexPath);

    try (Analyzer analyzer = new StandardAnalyzer()) {
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      try (IndexWriter indexWriter = new IndexWriter(directory, config)) {
        geonamesRecords.forEach(geonamesRecord -> index(indexWriter, geonamesRecord.name, geonamesRecord.alternatenames, geonamesRecord.countryCode, geonamesRecord.admin1Code, geonamesRecord.admin2Code, geonamesRecord.longitude, geonamesRecord.latitude));
      }
    }
    directory.close();
  }

  private void index(IndexWriter indexWriter, String name, String alternateNames, String countryCode, String admin1Code, String admin2Code, Double longitude, Double latitude) {
    try {
      Document doc = new Document();
      doc.add(new Field("name", name, TextField.TYPE_STORED));
      doc.add(new Field("alternatename", alternateNames, TextField.TYPE_STORED));
      doc.add(new Field("countryCode", countryCode, TextField.TYPE_STORED));
      doc.add(new Field("admin1Code", admin1Code, TextField.TYPE_STORED));
      doc.add(new Field("admin2Code", admin2Code, TextField.TYPE_STORED));
      doc.add(new StoredField("longitude", longitude));
      doc.add(new StoredField("latitude", latitude));
      indexWriter.addDocument(doc);
    } catch (Exception e) {
      throw new RuntimeException("An error occurred while creating Lucene document for Geonames data");
    }

  }

  public void search(String query) throws IOException, ParseException {
    try (Analyzer analyzer = new StandardAnalyzer()) {
      Directory directory = FSDirectory.open(indexPath);
      DirectoryReader indexReader = DirectoryReader.open(directory);
      IndexSearcher indexSearcher = new IndexSearcher(indexReader);


      ScoreDoc[] hits = indexSearcher.search(booleanQueryBuilder.build(), 10).scoreDocs;

      indexReader.close();
      directory.close();
    }
  }

}
