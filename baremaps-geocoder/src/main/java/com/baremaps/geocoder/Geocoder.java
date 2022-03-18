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

import com.baremaps.baremaps.geonames.Geonames;
import com.baremaps.baremaps.geonames.GeonamesRecord;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Geocoder {
  private Geonames geonames;
  private GeocoderLucene geocoderLucene;

  public Geocoder(Geonames geonames, Path indexPath) throws IOException {
    this.geonames = geonames;
    this.geocoderLucene = new GeocoderLucene(indexPath);
  }

  public void indexOsmData(InputStream inputStream) {}

  public void indexGeonamesData(InputStream inputStream) throws IOException {
    Stream<GeonamesRecord> geonamesRecords = this.geonames.parse(inputStream);
    geocoderLucene.indexGeonames(geonamesRecords);
  }
}
