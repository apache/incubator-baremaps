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

package org.apache.baremaps.geocoder.geonames;



import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.baremaps.geocoder.Geocoder;
import org.apache.baremaps.geocoder.IsoCountriesUtils;
import org.apache.baremaps.geocoder.Request;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class GeonamesGeocoder extends Geocoder {

  private Path data;

  public GeonamesGeocoder(Path index, Path data) throws IOException {
    super(index);
    this.data = data;
  }

  @Override
  protected Analyzer analyzer() {
    return new StandardAnalyzer();
  }

  @Override
  protected Stream<Document> documents() throws IOException {
    CsvMapper mapper = new CsvMapper();
    CsvSchema schema = CsvSchema.builder().addColumn("geonameid").addColumn("name")
        .addColumn("asciiname").addColumn("alternatenames").addColumn("latitude")
        .addColumn("longitude").addColumn("featureClass").addColumn("featureCode")
        .addColumn("countryCode").addColumn("cc2").addColumn("admin1Code").addColumn("admin2Code")
        .addColumn("admin3Code").addColumn("admin4Code").addColumn("population")
        .addColumn("elevation").addColumn("dem").addColumn("timezone").addColumn("modificationDate")
        .build().withColumnSeparator('\t').withoutQuoteChar();
    MappingIterator<GeonamesRecord> it =
        mapper.readerFor(GeonamesRecord.class).with(schema).readValues(data.toFile());
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false).map(record -> {
      Document document = new Document();
      document.add(new TextField("name", record.getName(), Store.YES));
      document.add(new TextField("country", IsoCountriesUtils.getCountry(record.getCountryCode()),
          Store.YES));
      document.add(new StringField("countryCode", record.getCountryCode(), Store.YES));
      document.add(new StoredField("longitude", record.getLongitude()));
      document.add(new StoredField("latitude", record.getLatitude()));
      document.add(new StoredField("asciiname", record.getLatitude()));
      document.add(new StoredField("alternatenames", record.getLatitude()));
      document.add(new StoredField("featureClass", record.getLatitude()));
      document.add(new StoredField("featureCode", record.getLatitude()));
      document.add(new StoredField("cc2", record.getLatitude()));
      document.add(new StoredField("cc2", record.getLatitude()));
      document.add(new StoredField("admin1Code", record.getLatitude()));
      document.add(new StoredField("admin2Code", record.getLatitude()));
      document.add(new StoredField("admin3Code", record.getLatitude()));
      document.add(new StoredField("admin4Code", record.getLatitude()));
      document.add(new StoredField("population", record.getLatitude()));
      document.add(new StoredField("elevation", record.getLatitude()));
      document.add(new StoredField("dem", record.getLatitude()));
      document.add(new StoredField("timezone", record.getLatitude()));
      document.add(new StoredField("modificationDate", record.getLatitude()));
      return document;
    });
  }

  @Override
  protected Query query(Analyzer analyzer, Request request) throws ParseException {
    BooleanQuery.Builder builder = new Builder();
    String query = QueryParser.escape(request.query());
    if (!query.isBlank()) {
      SimpleQueryParser nameQueryParser = new SimpleQueryParser(analyzer, "name");
      builder.add(nameQueryParser.parse(query), Occur.SHOULD);

      SimpleQueryParser countryQueryParser = new SimpleQueryParser(analyzer, "country");
      builder.add(countryQueryParser.parse(query), Occur.SHOULD);

      if (request.countryCode() != null) {
        builder.add(
            new TermQuery(new Term("countryCode", QueryParser.escape(request.countryCode()))),
            Occur.MUST);
      }
    }
    return builder.build();
  }
}
