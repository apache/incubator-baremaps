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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.baremaps.iploc.IpLoc;
import org.apache.baremaps.iploc.data.IpLocStats;
import org.apache.baremaps.iploc.nic.NicParser;
import org.apache.baremaps.stream.StreamException;
import org.apache.baremaps.utils.SqliteUtils;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record CreateIplocIndex(
    Path geonamesIndexPath,
    List<Path> nicPaths,
    Path targetIplocIndexPath) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(CreateIplocIndex.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    try (
        var directory = MMapDirectory.open(geonamesIndexPath);
        var searcherManager = new SearcherManager(directory, new SearcherFactory())) {
      logger.info("Creating the Iploc database");
      String jdbcUrl = String.format("JDBC:sqlite:%s", targetIplocIndexPath);

      SqliteUtils.executeResource(jdbcUrl, "iploc_init.sql");
      IpLoc ipLoc = new IpLoc(jdbcUrl, searcherManager);

      logger.info("Generating NIC objects stream");
      nicPaths.stream().parallel().forEach(path -> {
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
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
              IpLoc measure
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
    }
  }
}
