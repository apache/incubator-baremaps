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

package org.apache.baremaps.iploc.nic;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A utility for fetching the data of Network Coordination Centers (NIC) in temporary files. */
public class NicFetcher {

  private static final Logger logger = LoggerFactory.getLogger(NicFetcher.class);

  /** A list of default urls. */
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
      "https://ftp.arin.net/pub/rr/arin.db.gz", "https://ftp.arin.net/pub/rr/arin-nonauth.db.gz",
      "https://ftp.lacnic.net/lacnic/dbase/lacnic.db.gz",
      "https://ftp.ripe.net/ripe/dbase/ripe.db.gz");

  /**
   * Fetches the default urls.
   *
   * @return the fetched urls
   */
  public Stream<Path> fetch() {
    return fetch(NIC_URLS);
  }

  /**
   * Fetches the provided urls.
   *
   * @param urls the urls to fetch
   * @return the fetched urls
   */
  public Stream<Path> fetch(List<String> urls) {
    return urls.stream().filter(url -> !url.isEmpty()).map(this::fetch);
  }

  /**
   * Fetches the provided url.
   *
   * @param url the url to fetch
   * @return the fetched url
   */
  public Path fetch(String url) {
    try (InputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
      File file = File.createTempFile("nic_", ".download", new File("."));
      file.deleteOnExit();
      long size = Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      logger.info(String.format("Download %s (%smb unzipped)", url, size >> 20));
      return file.toPath();
    } catch (Exception e) {
      throw new RuntimeException("Failed to download " + url);
    }
  }
}
