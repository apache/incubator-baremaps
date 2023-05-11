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

package org.apache.baremaps.iploc;


import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ripe.ipresource.IpResourceRange;
import org.apache.baremaps.geocoder.GeonamesQueryBuilder;
import org.apache.baremaps.utils.IsoCountriesUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.locationtech.jts.geom.Coordinate;

/** Generating pairs of IP address ranges and their locations into an SQLite database */
public class IpLocMapper implements Function<NicObject, Optional<IpLocObject>> {

  private final float SCORE_THRESHOLD = 0.1f;

  private final SearcherManager searcherManager;

  /**
   * Create a new IpLoc object
   *
   * @param searcherManager the geocoder that will be used to find the locations of the objects
   */
  public IpLocMapper(SearcherManager searcherManager) {
    this.searcherManager = searcherManager;
  }

  /**
   * Process an NicObject of type Inetnum the score is above a threshold the database
   *
   * @param nicObject the nicObject
   * @return the optional inetnum location
   * @throws IOException
   * @throws ParseException
   */
  public Optional<IpLocObject> apply(NicObject nicObject) {
    try {
      if (nicObject.attributes().isEmpty()) {
        return Optional.empty();
      }

      NicAttribute firstAttribute = nicObject.attributes().get(0);
      if (!Objects.equals(firstAttribute.name(), "inetnum")) {
        return Optional.empty();
      }

      IpResourceRange ipRange = IpResourceRange.parse(firstAttribute.value());
      var start = InetAddresses.forString(ipRange.getStart().toString());
      var end = InetAddresses.forString(ipRange.getEnd().toString());


      Map<String, String> attributes = nicObject.toMap();

      // Use a default name if there is no netname
      String network = attributes.getOrDefault("netname", "unknown");

      // If there is a geoloc field, we use the latitude and longitude provided
      if (attributes.containsKey("geoloc")) {
        Optional<Coordinate> location = stringToCoordinate(attributes.get("geoloc"));
        if (location.isPresent()) {
          return Optional.of(new IpLocObject(
              attributes.get("geoloc"),
              start,
              end,
              location.get(),
              network,
              attributes.get("country")));
        }
      }

      // If there is an address we use that address to query the geocoder
      if (attributes.containsKey("address")) {
        Optional<Coordinate> location =
            findLocation(attributes.get("address"), attributes.get("country"));
        if (location.isPresent()) {
          return Optional.of(new IpLocObject(
              attributes.get("address"),
              start,
              end,
              location.get(),
              network,
              attributes.get("country")));
        }
      }

      // If there is a description we use that description to query the geocoder
      if (attributes.containsKey("descr")) {
        Optional<Coordinate> location =
            findLocation(attributes.get("descr"), attributes.get("country"));
        if (location.isPresent()) {
          return Optional.of(new IpLocObject(
              attributes.get("descr"),
              start,
              end,
              location.get(),
              network,
              attributes.get("country")));
        }
      }

      // If there is a name we use that name to query the geocoder
      if (attributes.containsKey("name")) {
        Optional<Coordinate> location =
            findLocation(attributes.get("name"), attributes.get("country"));
        if (location.isPresent()) {
          return Optional.of(new IpLocObject(attributes.get("name"),
              start,
              end,
              location.get(),
              network, attributes.get("country")));
        }
      }

      // If there is a country that follows the ISO format we use that country's actual name from
      // the iso country map to query the geocoder
      if (attributes.containsKey("country")
          && IsoCountriesUtils.containsCountry(attributes.get("country").toUpperCase())) {
        String countryUppercase = attributes.get("country").toUpperCase();
        Optional<Coordinate> location =
            findLocation(IsoCountriesUtils.getCountry(countryUppercase), countryUppercase);
        if (location.isPresent()) {
          return Optional.of(new IpLocObject(
              IsoCountriesUtils.getCountry(countryUppercase),
              start,
              end,
              location.get(),
              network,
              countryUppercase));
        }
      }

      // If there is a country that did not follow the ISO format we will query using the country
      // has plain text
      if (attributes.containsKey("country")) {
        Optional<Coordinate> location = findLocation(attributes.get("country"), "");
        if (location.isPresent()) {
          return Optional.of(new IpLocObject(
              attributes.get("country"),
              start,
              end,
              location.get(),
              network,
              attributes.get("country")));
        }
      }

      return Optional.empty();

    } catch (IOException | ParseException e) {
      return Optional.empty();
    }
  }

  /**
   * Use the geocoder to find a latitude/longitude with the given query.
   *
   * @param searchTerms the search terms
   * @param countryCode the country code filter
   * @return an optional of the location
   * @throws IOException
   * @throws ParseException
   */
  private Optional<Coordinate> findLocation(String searchTerms, String countryCode)
      throws IOException, ParseException {
    var indexSearcher = searcherManager.acquire();
    var geonamesQuery =
        new GeonamesQueryBuilder().queryText(searchTerms).countryCode(countryCode).build();

    TopDocs topDocs = indexSearcher.search(geonamesQuery, 1);
    if (topDocs.scoreDocs.length == 0) {
      return Optional.empty();
    }

    var scoreDoc = topDocs.scoreDocs[0];
    if (scoreDoc.score < SCORE_THRESHOLD) {
      return Optional.empty();
    }

    var document = indexSearcher.doc(scoreDoc.doc);
    double latitude = document.getField("latitude").numericValue().doubleValue();
    double longitude = document.getField("longitude").numericValue().doubleValue();
    return Optional.of(new Coordinate(latitude, longitude));
  }

  /**
   * Parse the geoloc in the given string and insert it in the database. The given geoloc is
   * represented by two doubles split by a space.
   *
   * @param geoloc the latitude/longitude coordinates in a string
   * @return an optional containing the location
   */
  private Optional<Coordinate> stringToCoordinate(String geoloc) {
    String doubleRegex = "(\\d+\\.\\d+)";
    Pattern pattern = Pattern.compile("^" + doubleRegex + " " + doubleRegex + "$");
    Matcher matcher = pattern.matcher(geoloc);
    if (matcher.find()) {
      double latitude = Double.parseDouble(matcher.group(1));
      double longitude = Double.parseDouble(matcher.group(2));
      return Optional.of(new Coordinate(latitude, longitude));
    }
    return Optional.empty();
  }
}
