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

package org.apache.baremaps.cli.iploc;



import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import org.apache.baremaps.geocoder.geonames.GeonamesGeocoder;
import org.apache.baremaps.iploc.IpLoc;
import org.apache.baremaps.iploc.data.IpLocStats;
import org.apache.baremaps.iploc.database.SqliteUtils;
import org.apache.baremaps.iploc.nic.NicObject;
import org.apache.baremaps.iploc.nic.NicParser;
import org.apache.baremaps.workflow.Step;
import org.apache.baremaps.workflow.Workflow;
import org.apache.baremaps.workflow.WorkflowExecutor;
import org.apache.baremaps.workflow.tasks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "init", description = "Generate the IpLoc database.")
public class Init implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Init.class);

  @Option(names = {"--index"}, paramLabel = "INDEX", description = "The path to the lucene index.",
      defaultValue = "geocoder_index")
  private Path index;

  @Option(names = {"--database"}, paramLabel = "DATABASE",
      description = "The path of the output SQLite database.", defaultValue = "iploc.db")
  private Path database;

  public static final String GEONAMES_URL =
      "https://download.geonames.org/export/dump/allCountries.zip";

  public static final List<String> NIC_URLS = List.of(
      "https://ftp.afrinic.net/pub/dbase/afrinic.db.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.as-block.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.as-set.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.domain.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.filter-set.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.inet-rtr.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.inet6num.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.inetnum.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.irt.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.key-cert.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.limerick.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.mntner.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.organisation.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.peering-set.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.role.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.route-set.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.route.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.route6.gz",
      "https://ftp.apnic.net/apnic/whois/apnic.db.rtr-set.gz",
      "https://ftp.arin.net/pub/rr/arin.db.gz", "https://ftp.lacnic.net/lacnic/dbase/lacnic.db.gz",
      "https://ftp.ripe.net/ripe/dbase/ripe.db.gz");

  @Override
  public Integer call() throws Exception {

    Step downloadGeonamesStep = new Step("fetch-geonames-allcountries", List.of(),
        List.of(new DownloadUrl(GEONAMES_URL, "downloads/geonames-allcountries.zip", true),
            new UnzipFile("downloads/geonames-allcountries.zip", "archives")));

    Stream<Step> downloadNicSteps =
        NIC_URLS.stream()
            .map(nicUrl -> new Step(String.format("fetch-%s", nicUrl), List.of(),
                List.of(
                    new DownloadUrl(nicUrl,
                        String.format("downloads/%s",
                            nicUrl.substring(nicUrl.lastIndexOf("/") + 1)),
                        true),
                    new UngzipFile(String.format("downloads/%s",
                        nicUrl.substring(nicUrl.lastIndexOf("/") + 1)), "archives"))));

    var workflow =
        new Workflow(Stream.concat(Stream.of(downloadGeonamesStep), downloadNicSteps).toList());

    try (var workflowExecutor = new WorkflowExecutor(workflow)) {
      workflowExecutor.execute().join();
    } catch (Exception e) {
      logger.error("Workflow execution fail");
      throw (e);
    }

    try (GeonamesGeocoder geocoder =
        new GeonamesGeocoder(index, Path.of("archives/geonames-allcountries.txt"))) {
      if (!geocoder.indexExists()) {
        logger.info("Building the geocoder index");
        geocoder.build();
      }

      logger.info("Loading the geocoder index");
      geocoder.open();

      logger.info("Generating NIC objects stream");
      Stream<NicObject> fetchNicObjectStream = NIC_URLS.stream().flatMap(nicUrl -> {
        try {
          String nicFileName = nicUrl.substring(nicUrl.lastIndexOf("/") + 1);
          String nicFileNameUncompressed = nicFileName.substring(0, nicFileName.length() - 3);
          InputStream inputStream = new BufferedInputStream(
              Files.newInputStream(Path.of(String.format("archives/%s", nicFileNameUncompressed))));
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
      String jdbcUrl = String.format("JDBC:sqlite:%s", database.toString());
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
    }
    return 0;
  }
}
