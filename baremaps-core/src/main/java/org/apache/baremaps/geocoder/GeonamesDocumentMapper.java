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



import java.util.function.Function;
import org.apache.lucene.document.*;

/**
 * Maps a {@link GeonamesRecord} to a Lucene {@link Document}.
 */
public class GeonamesDocumentMapper implements Function<GeonamesRecord, Document> {

  @Override
  public Document apply(GeonamesRecord record) {
    Document document = new Document();
    document.add(new TextField("name", record.getName(), Field.Store.YES));
    document.add(new TextField("country", IsoCountriesUtils.getCountry(record.getCountryCode()),
        Field.Store.YES));
    document.add(new StringField("countryCode", record.getCountryCode(), Field.Store.YES));
    document.add(new LatLonPoint("point", record.getLatitude(), record.getLongitude()));
    document.add(new StoredField("longitude", record.getLongitude()));
    document.add(new StoredField("latitude", record.getLatitude()));
    document.add(new StoredField("asciiname", record.getAsciiname()));
    document.add(new StoredField("alternatenames", record.getAlternatenames()));
    document.add(new StoredField("featureClass", record.getFeatureClass()));
    document.add(new StoredField("featureCode", record.getFeatureCode()));
    document.add(new StoredField("cc2", record.getCc2()));
    document.add(new StoredField("admin1Code", record.getAdmin1Code()));
    document.add(new StoredField("admin2Code", record.getAdmin2Code()));
    document.add(new StoredField("admin3Code", record.getAdmin3Code()));
    document.add(new StoredField("admin4Code", record.getAdmin4Code()));
    document.add(new StoredField("population", record.getPopulation()));
    if (record.getElevation() != null) {
      document.add(new StoredField("elevation", record.getElevation()));
    }
    document.add(new StoredField("dem", record.getDem()));
    document.add(new StoredField("timezone", record.getTimezone()));
    document.add(new StoredField("modificationDate", record.getModificationDate()));
    return document;
  }
}
