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

package com.baremaps.iploc;

import com.baremaps.geocoder.Geocoder;
import com.baremaps.geocoder.Request;
import com.baremaps.geocoder.Response;
import com.baremaps.iploc.nic.NicAttribute;
import com.baremaps.iploc.nic.NicObject;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.lucene.queryparser.classic.ParseException;

/** Generating pairs of IP address ranges and their locations into an SQLite database */
public class IpLoc {

  private final Dao<InetnumLocation> inetnumLocationDao;
  private final Geocoder geocoder;

  /**
   * Create a new IpLoc object
   *
   * @param databaseUrl the jdbc url to the sqlite database
   * @param geocoder the geocoder that will be used to find the locations of the objects
   */
  public IpLoc(String databaseUrl, Geocoder geocoder) {
    inetnumLocationDao = new InetnumLocationDaoImpl(databaseUrl);
    this.geocoder = geocoder;
  }

  /**
   * Insert the nic objects into the Iploc database. Only inetnum NIC Objects are supported for now.
   * The type of the object is defined by the key of the first attribute in the NIC object.
   *
   * @param nicObjects the stream of nic objects to import
   */
  public void insertNicObjects(Stream<NicObject> nicObjects) {
    nicObjects.forEach(
        nicObject -> {
          if (nicObject.attributes().size() > 0) {
            NicAttribute firstAttribute = nicObject.attributes().get(0);
            if (Objects.equals(firstAttribute.name(), "inetnum")) {
              try {
                processInetnum(nicObject, firstAttribute);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }
          }
        });
  }

  /**
   * Process an NicObject of type Inetnum Todo: Add the country for every query Todo: Only insert if
   * the score is above a threshold Todo: Insert the address that was used for the geocoding into
   * the database
   *
   * @param nicObject the nicObject
   * @param firstAttribute the first attribute of the NicObject which should contain the IP range
   * @throws IOException
   * @throws ParseException
   */
  private void processInetnum(NicObject nicObject, NicAttribute firstAttribute)
      throws IOException, ParseException {

    Ipv4Range ipRange = new Ipv4Range(firstAttribute.value());
    Map<String, String> attributes = nicObject.toMap();

    // Use a default name if there is no netname
    String name = attributes.getOrDefault("netname", "unknown");

    // If there is a geoloc field, we use the lat lon provided
    if (attributes.containsKey("geoloc")
        && insertInetnumFromGeoloc(name, ipRange, attributes.get("geoloc"))) {
      return;
    }
    // If there is an address we use that location to query geonames
    if (attributes.containsKey("address")
        && findAndInsertInetnumLocationFromQuery(name, ipRange, attributes.get("address"))) {
      return;
    }
    // If there is a description we use that location to query geonames
    if (attributes.containsKey("descr")
        && findAndInsertInetnumLocationFromQuery(name, ipRange, attributes.get("descr"))) {
      return;
    }
    // If there is a country
    if (attributes.containsKey("country")
        && findAndInsertInetnumLocationFromQuery(name, ipRange, attributes.get("country"))) {
      return;
    }
  }

  /**
   * Use the geocoder to find a latitude/longitude with the given query. If one is found, insert the
   * inetnum location into the database.
   *
   * @param ipRange the range of IPs
   * @param query the query for the geocoder
   * @return true if a location was found and a row was inserted
   * @throws IOException
   * @throws ParseException
   */
  private boolean findAndInsertInetnumLocationFromQuery(
      String name, Ipv4Range ipRange, String query) throws IOException, ParseException {
    Response response = geocoder.search(new Request(query, 1));
    if (response.results().size() > 0) {
      double latitude = Double.parseDouble(response.results().get(0).document().get("latitude"));
      double longitude = Double.parseDouble(response.results().get(0).document().get("longitude"));
      inetnumLocationDao.save(new InetnumLocation(name, ipRange, latitude, longitude));
      return true;
    }
    return false;
  }

  /**
   * Parse the geoloc in the given string and insert it in the database. The given geoloc is
   * represented by two doubles split by a space.
   *
   * @param ipRange the range of IPs
   * @param geoloc the latitude/longitude coordinates
   * @return true if the given location was valid
   */
  private boolean insertInetnumFromGeoloc(String name, Ipv4Range ipRange, String geoloc) {
    String doubleRegex = "(\\d+\\.\\d+)";
    Pattern pattern = Pattern.compile("^" + doubleRegex + " " + doubleRegex + "$");
    Matcher matcher = pattern.matcher(geoloc);
    if (matcher.find()) {
      double latitude = Double.parseDouble(matcher.group(1));
      double longitude = Double.parseDouble(matcher.group(2));
      inetnumLocationDao.save(new InetnumLocation(name, ipRange, latitude, longitude));
      return true;
    }
    return false;
  }
}
