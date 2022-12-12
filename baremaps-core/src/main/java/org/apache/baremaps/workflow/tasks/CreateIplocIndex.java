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
import org.apache.baremaps.iploc.nic.NicParser;
import org.apache.baremaps.stream.StreamException;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record CreateIplocIndex(String geonamesIndexPath, List<String> nicPaths,
                               String targetIplocIndexPath) implements Task {

    private static final Logger logger = LoggerFactory.getLogger(CreateIplocIndex.class);

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Generating Iploc from {} {}", geonamesIndexPath, nicPaths);

        logger.info("Creating the Geocoder");
        GeonamesGeocoder geocoder;
        try {
            geocoder = new GeonamesGeocoder(Path.of(geonamesIndexPath), null);
            if (!geocoder.indexExists()) {
                logger.error("Geocoder index doesn't exist");
                return;
            }
            geocoder.open();
        } catch (Exception e) {
            logger.error("Error while creating the geocoder index", e);
            return;
        }

        logger.info("Creating the Iploc database");
        String jdbcUrl = String.format("JDBC:sqlite:%s", targetIplocIndexPath);

        SqliteUtils.executeResource(jdbcUrl, "iploc_init.sql");
        IpLoc ipLoc = new IpLoc(jdbcUrl, geocoder);

        logger.info("Generating NIC objects stream");
        nicPaths.stream().parallel().forEach(path -> {
          try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(Path.of(path)));) {
            var nicObjects = NicParser.parse(inputStream);
            logger.info("Inserting the nic objects into the Iploc database");
            ipLoc.insertNicObjects(nicObjects);
          } catch (IOException e) {
            throw new StreamException(e);
          }
        });

        IpLocStats ipLocStats = ipLoc.getIplocStats();

        logger.info(
                """
                        IpLoc stats
                        -----------
                        inetnumInsertedByAddress : {}
                        inetnumInsertedByDescr : {}
                        inetnumInsertedByCountry : {}
                        inetnumInsertedByCountryCode : {}
                        inetnumInsertedByGeoloc : {}
                        inetnumNotInserted : {}""",
                ipLocStats.getInsertedByAddressCount(), ipLocStats.getInsertedByDescrCount(),
                ipLocStats.getInsertedByCountryCount(), ipLocStats.getInsertedByCountryCodeCount(),
                ipLocStats.getInsertedByGeolocCount(), ipLocStats.getNotInsertedCount());

        logger.info("IpLoc database created successfully");

        logger.info("Finished creating the Geocoder index {}", targetIplocIndexPath);
    }
}
