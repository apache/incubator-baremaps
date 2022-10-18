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



import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.baremaps.geocoder.Geocoder;
import org.apache.baremaps.geocoder.IsoCountriesUtils;
import org.apache.baremaps.geocoder.Request;
import org.apache.baremaps.geocoder.Response;
import org.apache.baremaps.iploc.data.InetnumLocation;
import org.apache.baremaps.iploc.data.IpLocStats;
import org.apache.baremaps.iploc.data.Ipv4Range;
import org.apache.baremaps.iploc.data.Location;
import org.apache.baremaps.iploc.database.InetnumLocationDao;
import org.apache.baremaps.iploc.database.InetnumLocationDaoSqliteImpl;
import org.apache.baremaps.iploc.nic.NicAttribute;
import org.apache.baremaps.iploc.nic.NicObject;
import org.apache.baremaps.stream.StreamUtils;
import org.apache.lucene.queryparser.classic.ParseException;

/** Generating pairs of IP address ranges and their locations into an SQLite database */
public class IpLoc {

  private final float SCORE_THRESHOLD = 0.1f;

  private final InetnumLocationDao inetnumLocationDao;
  private final Geocoder geocoder;
  private IpLocStats iplocStats;

  /**
   * Create a new IpLoc object
   *
   * @param databaseUrl the jdbc url to the sqlite database
   * @param geocoder the geocoder that will be used to find the locations of the objects
   */
  public IpLoc(String databaseUrl, Geocoder geocoder) {
    inetnumLocationDao = new InetnumLocationDaoSqliteImpl(databaseUrl);
    iplocStats = new IpLocStats();
    this.geocoder = geocoder;
  }

  /**
   * Insert the nic objects into the Iploc database. Only inetnum NIC Objects are supported for now.
   * The type of the object is defined by the key of the first attribute in the NIC object.
   *
   * @param nicObjects the stream of nic objects to import
   */
  public void insertNicObjects(Stream<NicObject> nicObjects) {
    StreamUtils.partition(nicObjects.filter(this::isInetnum).map(this::nicObjectToInetnumLocation)
        // TODO: we should probably not filter, i.e., even in the worst case we should have
        // the country
        // Cache the list of country
        .filter(Optional::isPresent).map(Optional::get), 100)
        .map(partition -> partition.collect(Collectors.toList())).forEach(inetnumLocationDao::save);
  }

  private boolean isInetnum(NicObject nicObject) {
    return "inetnum".equals(nicObject.type());
  }

  /**
   * Process an NicObject of type Inetnum the score is above a threshold the database
   *
   * @param nicObject the nicObject
   * @return the optional inetnum location
   * @throws IOException
   * @throws ParseException
   */
  private Optional<InetnumLocation> nicObjectToInetnumLocation(NicObject nicObject) {
    try {
      if (nicObject.attributes().isEmpty()) {
        return Optional.empty();
      }

      NicAttribute firstAttribute = nicObject.attributes().get(0);

      if (!Objects.equals(firstAttribute.name(), "inetnum")) {
        return Optional.empty();
      }

      Ipv4Range ipRange = new Ipv4Range(firstAttribute.value());
      Map<String, String> attributes = nicObject.toMap();

      // Use a default name if there is no netname
      String network = attributes.getOrDefault("netname", "unknown");

      // If there is a geoloc field, we use the latitude and longitude provided
      if (attributes.containsKey("geoloc")) {
        Optional<Location> location = stringToLocation(attributes.get("geoloc"));
        if (location.isPresent()) {
          iplocStats.incrementInsertedByGeolocCount();
          return Optional.of(new InetnumLocation(attributes.get("geoloc"), ipRange, location.get(),
              network, attributes.get("country")));
        }
      }
      // If there is an address we use that address to query the geocoder
      if (attributes.containsKey("address")) {
        Optional<Location> location =
            findLocation(new Request(attributes.get("address"), 1, attributes.get("country")));
        if (location.isPresent()) {
          iplocStats.incrementInsertedByAddressCount();
          return Optional.of(new InetnumLocation(attributes.get("address"), ipRange, location.get(),
              network, attributes.get("country")));
        }
      }
      // If there is a description we use that description to query the geocoder
      if (attributes.containsKey("descr")) {
        Optional<Location> location =
            findLocation(new Request(attributes.get("descr"), 1, attributes.get("country")));
        if (location.isPresent()) {
          iplocStats.incrementInsertedByDescrCount();
          return Optional.of(new InetnumLocation(attributes.get("descr"), ipRange, location.get(),
              network, attributes.get("country")));
        }
      }
      // If there is a name we use that name to query the geocoder
      if (attributes.containsKey("name")) {
        Optional<Location> location =
            findLocation(new Request(attributes.get("name"), 1, attributes.get("country")));
        if (location.isPresent()) {
          iplocStats.incrementInsertedByDescrCount();
          return Optional.of(new InetnumLocation(attributes.get("name"), ipRange, location.get(),
              network, attributes.get("country")));
        }
      }
      // If there is a country that is follow the ISO format we use that country's actual name from
      // the iso country map to query the geocoder
      if (attributes.containsKey("country")
          && IsoCountriesUtils.containsCountry(attributes.get("country").toUpperCase())) {
        String countryUppercase = attributes.get("country").toUpperCase();
        Optional<Location> location = findLocation(
            new Request(IsoCountriesUtils.getCountry(countryUppercase), 1, countryUppercase));
        if (location.isPresent()) {
          iplocStats.incrementInsertedByCountryCodeCount();
          return Optional.of(new InetnumLocation(IsoCountriesUtils.getCountry(countryUppercase),
              ipRange, location.get(), network, countryUppercase));
        }
      }

      // If there is a country that did not follow the ISO format we will query using the country
      // has plain text
      if (attributes.containsKey("country")) {
        Optional<Location> location = findLocation(new Request(attributes.get("country"), 1));
        if (location.isPresent()) {
          iplocStats.incrementInsertedByCountryCount();
          return Optional.of(new InetnumLocation(attributes.get("country"), ipRange, location.get(),
              network, attributes.get("country")));
        }
      }

      iplocStats.incrementNotInsertedCount();
      return Optional.empty();

    } catch (IOException | ParseException e) {
      return Optional.empty();
    }
  }

  /**
   * Use the geocoder to find a latitude/longitude with the given query.
   *
   * @param request for the location
   * @return an optional of the location
   * @throws IOException
   * @throws ParseException
   */
  private Optional<Location> findLocation(Request request) throws IOException, ParseException {
    Response response = geocoder.search(request);
    if (response.results().size() > 0) {
      if (response.topDocs().scoreDocs[0].score > SCORE_THRESHOLD) {
        double latitude = Double.parseDouble(response.results().get(0).document().get("latitude"));
        double longitude =
            Double.parseDouble(response.results().get(0).document().get("longitude"));
        return Optional.of(new Location(latitude, longitude));
      }
    }
    return Optional.empty();
  }

  /**
   * Parse the geoloc in the given string and insert it in the database. The given geoloc is
   * represented by two doubles split by a space.
   *
   * @param geoloc the latitude/longitude coordinates in a string
   * @return an optional containing the location
   */
  private Optional<Location> stringToLocation(String geoloc) {
    String doubleRegex = "(\\d+\\.\\d+)";
    Pattern pattern = Pattern.compile("^" + doubleRegex + " " + doubleRegex + "$");
    Matcher matcher = pattern.matcher(geoloc);
    if (matcher.find()) {
      double latitude = Double.parseDouble(matcher.group(1));
      double longitude = Double.parseDouble(matcher.group(2));
      return Optional.of(new Location(latitude, longitude));
    }
    return Optional.empty();
  }

  public IpLocStats getIplocStats() {
    return iplocStats;
  }
}
