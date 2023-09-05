package org.apache.baremaps.geocoderosm;

import org.apache.baremaps.geocoder.GeocoderConstants;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

public class GeocoderOSMQuery {

  private final String query;

  public GeocoderOSMQuery(String query) {
    this.query = query;
  }

  public Query build() {
    var builder = new BooleanQuery.Builder();
    var queryTextEsc = QueryParser.escape(query);

    var parser = new SimpleQueryParser(GeocoderConstants.ANALYZER, OSMTags.NAME.key());
    var termsQuery = parser.parse(queryTextEsc);
    // at least one terms of the queryText must be present
    builder.add(termsQuery, BooleanClause.Occur.MUST);
    return builder.build();
  }
}
