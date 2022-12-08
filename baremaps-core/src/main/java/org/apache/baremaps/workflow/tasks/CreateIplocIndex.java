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

package org.apache.baremaps.workflow.tasks;

import org.apache.baremaps.geocoder.geonames.GeonamesGeocoder;
import org.apache.baremaps.iploc.IpLoc;
import org.apache.baremaps.iploc.data.IpLocStats;
import org.apache.baremaps.iploc.database.SqliteUtils;
import org.apache.baremaps.iploc.nic.NicObject;
import org.apache.baremaps.iploc.nic.NicParser;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public record CreateIplocIndex(String geonamesIndexPath, String[] iplocNicPath, String targetIplocIndexPath) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(CreateIplocIndex.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    logger.info("Generating Iploc from {} {}", geonamesIndexPath, iplocNicPath);
    try (GeonamesGeocoder geocoder =
                 new GeonamesGeocoder(Path.of(geonamesIndexPath), null)) {
      if (!geocoder.indexExists()) {
        logger.error("Geocoder index doesn't exist");
        return;
      }

      geocoder.open();

      logger.info("Generating NIC objects stream");
      Stream<NicObject> fetchNicObjectStream = Arrays.stream(iplocNicPath).flatMap(iplocNicPath -> {
        try {
          InputStream inputStream = new BufferedInputStream(
                  Files.newInputStream(Path.of(iplocNicPath)));
          return NicParser.parse(inputStream).onClose(() -> {
            try {
              inputStream.close();
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });

      logger.info("Creating the Iploc database");
      String jdbcUrl = String.format("JDBC:sqlite:%s", targetIplocIndexPath);
      try {
        SqliteUtils.executeResource(jdbcUrl, "iploc_init.sql");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      logger.info("Inserting the nic objects into the Iploc database");
      org.apache.baremaps.iploc.IpLoc ipLoc = new IpLoc(jdbcUrl, geocoder);
      ipLoc.insertNicObjects(fetchNicObjectStream);
      IpLocStats ipLocStats = ipLoc.getIplocStats();

      logger.info(String.format(
              "IpLoc stats\n" + "-----------\n" + "inetnumInsertedByAddress : %s\n"
                      + "inetnumInsertedByDescr : %s\n" + "inetnumInsertedByCountry : %s\n"
                      + "inetnumInsertedByCountryCode : %s\n" + "inetnumInsertedByGeoloc : %s\n"
                      + "inetnumNotInserted : %s\n",
              ipLocStats.getInsertedByAddressCount(), ipLocStats.getInsertedByDescrCount(),
              ipLocStats.getInsertedByCountryCount(), ipLocStats.getInsertedByCountryCodeCount(),
              ipLocStats.getInsertedByGeolocCount(), ipLocStats.getNotInsertedCount()));

      logger.info("IpLoc database created successfully");
    } catch(Exception e) {
      logger.error("Error while creating the geocoder index", e);
      throw(e);
    }
    logger.info("Finished creating the Geocoder index {}", targetIplocIndexPath);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateIplocIndex that = (CreateIplocIndex) o;
    return Objects.equals(geonamesIndexPath, that.geonamesIndexPath) && Arrays.equals(iplocNicPath, that.iplocNicPath) && Objects.equals(targetIplocIndexPath, that.targetIplocIndexPath);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(geonamesIndexPath, targetIplocIndexPath);
    result = 31 * result + Arrays.hashCode(iplocNicPath);
    return result;
  }

  @Override
  public String toString() {
    return "CreateIplocIndex{" +
            "geonamesIndexPath='" + geonamesIndexPath + '\'' +
            ", iplocNicPath=" + Arrays.toString(iplocNicPath) +
            ", targetIplocIndexPath='" + targetIplocIndexPath + '\'' +
            '}';
  }
}
