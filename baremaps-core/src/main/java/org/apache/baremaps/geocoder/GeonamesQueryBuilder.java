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



import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Utility class to build a Lucene {@link Query}.
 */
public class GeonamesQueryBuilder {

  private final Analyzer analyzer;

  private String queryText;

  private String countryCode = "";

  public GeonamesQueryBuilder() {
    this(GeocoderConstants.ANALYZER);
  }

  public GeonamesQueryBuilder(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  public GeonamesQueryBuilder queryText(String queryText) {
    this.queryText = queryText;
    return this;
  }

  public GeonamesQueryBuilder countryCode(String countryCode) {
    this.countryCode = countryCode;
    return this;
  }

  public Query build() {
    var builder = new BooleanQuery.Builder();

    if (queryText != null) {
      var queryTextEsc = QueryParser.escape(queryText);
      if (!queryTextEsc.isBlank()) {
        var fieldWeights = Map.of("name", 1f, "country", 1f);
        var termsQuery = new SimpleQueryParser(analyzer, fieldWeights).parse(queryTextEsc);
        builder.add(termsQuery, BooleanClause.Occur.SHOULD);
      }
    }

    if (countryCode != null) {
      var countryCodeEsc = QueryParser.escape(countryCode);
      if (!countryCodeEsc.isBlank()) {
        var countryCodeQuery = new TermQuery(new Term("countryCode", countryCodeEsc));
        builder.add(countryCodeQuery, BooleanClause.Occur.MUST);
      }
    }

    return builder.build();
  }
}
