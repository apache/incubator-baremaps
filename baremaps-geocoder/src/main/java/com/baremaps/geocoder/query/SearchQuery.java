/*
 * Copyright (C) 2021 Heig-vd
 *
 * Licensed under the “Commons Clause” License Condition v1.0. You may obtain a copy of the License at
 *
 * https://github.com/heigvd-software-engineering/netscan/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baremaps.geocoder.query;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

/** Search query used by Geocoders. Can be unstructured address or even places */
public abstract class SearchQuery {

  protected Query query;

  protected SearchQuery() {}

  /**
   * Get the search query.
   *
   * @return the search query
   */
  public Query getQuery() {
    return query;
  }

  /**
   * Parse String into a ISO 2-letter country code
   *
   * @param query The String to parse
   * @return a String with the code separeted by a
   */
  protected String countryQueryParser(String query) {
    String[] values = query.split("[ ,-]");
    return Arrays.stream(values)
        .map(QueryUtils::getCountryCode)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));
  }

  /**
   * Escape the input by escaping Lucene reserved keywords and symbols
   *
   * @param input The input string
   * @return a string with the char escaped
   */
  protected String escape(String input) {
    return QueryParser.escape(input).replaceAll("\\b(AND|OR|NOT)\\b", "\\$1");
  }
}
