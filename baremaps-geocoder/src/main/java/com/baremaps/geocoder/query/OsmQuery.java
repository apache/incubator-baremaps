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

package com.baremaps.geocoder.query;

import com.baremaps.geocoder.analyser.BaseAnalyser;
import com.baremaps.geocoder.analyser.CityAnalyser;
import com.baremaps.geocoder.analyser.CountryAnalyser;
import com.baremaps.geocoder.analyser.StreetAnalyser;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;

public class OsmQuery extends SearchQuery {

  public OsmQuery(
      float streetWeight,
      float housenumberWeight,
      float cityWeight,
      float postcodeWeight,
      float stateWeight,
      float countryWeight,
      String query) {

    query = escape(query);
    BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

    Map<String, Analyzer> analyzerPerField = new HashMap<>();
    analyzerPerField.put("city", new CityAnalyser());
    analyzerPerField.put("country_name", new CountryAnalyser());
    analyzerPerField.put("country", new CountryAnalyser());
    analyzerPerField.put("street", new StreetAnalyser());

    try (Analyzer analyzer = new PerFieldAnalyzerWrapper(new BaseAnalyser(), analyzerPerField)) {
      booleanQueryBuilder.add(
          new BoostQuery(new QueryParser("street", analyzer).parse(query), streetWeight),
          Occur.SHOULD);

      booleanQueryBuilder.add(
          new BoostQuery(new QueryParser("housenumber", analyzer).parse(query), housenumberWeight),
          Occur.SHOULD);

      booleanQueryBuilder.add(
          new BoostQuery(new QueryParser("city", analyzer).parse(query), cityWeight), Occur.SHOULD);

      booleanQueryBuilder.add(
          new BoostQuery(new QueryParser("state", analyzer).parse(query), stateWeight),
          Occur.SHOULD);

      booleanQueryBuilder.add(
          new BoostQuery(new QueryParser("postcode", analyzer).parse(query), postcodeWeight),
          Occur.SHOULD);

      booleanQueryBuilder.add(
          new BoostQuery(new QueryParser("full", analyzer).parse(query), 1f), Occur.SHOULD);

      String countryQuery = countryQueryParser(query);
      if (countryQuery != null && !countryQuery.isEmpty()) {
        booleanQueryBuilder.add(
            new BoostQuery(new QueryParser("country", analyzer).parse(countryQuery), countryWeight),
            Occur.SHOULD);

        booleanQueryBuilder.add(
            new BoostQuery(
                new QueryParser("country_name", analyzer).parse(countryQuery), countryWeight),
            Occur.SHOULD);
      }
      booleanQueryBuilder.add(
          new BoostQuery(new QueryParser("country", analyzer).parse(query), countryWeight),
          Occur.SHOULD);
      booleanQueryBuilder.add(
          new BoostQuery(new QueryParser("country_name", analyzer).parse(query), countryWeight),
          Occur.SHOULD);

    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse query");
    }

    this.query = booleanQueryBuilder.build();
  }
}
