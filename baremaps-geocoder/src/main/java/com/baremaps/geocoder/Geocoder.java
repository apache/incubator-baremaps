package com.baremaps.geocoder;

import com.baremaps.baremaps.geonames.GeonamesRecord;
import com.baremaps.baremaps.geonames.Geonames;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Geocoder {
  private Geonames geonames;
  private GeocoderLucene geocoderLucene;

  public Geocoder(Geonames geonames, Path indexPath) throws IOException {
    this.geonames = geonames;
    this.geocoderLucene = new GeocoderLucene(indexPath);
  }

  public void indexOsmData(InputStream inputStream) {
  }

  public void indexGeonamesData(InputStream inputStream) throws IOException {
    Stream<GeonamesRecord> geonamesRecords = this.geonames.parse(inputStream);
    geocoderLucene.indexGeonames(geonamesRecords);
    }
}
