package com.baremaps.nic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

/**
 * A utility for fetching the data of Network Coordination Centers (NIC) in temporary files.
 */
public class NicFetcher {

  /**
   * A list of default urls.
   */
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
      "https://ftp.arin.net/pub/rr/arin.db.gz",
      "https://ftp.arin.net/pub/rr/arin-nonauth.db.gz",
      "https://ftp.lacnic.net/lacnic/dbase/lacnic.db.gz",
      "https://ftp.ripe.net/ripe/dbase/ripe.db.gz"
  );

  /**
   * Fetches the default urls.
   * @return the fetched urls
   * @throws IOException
   */
  public Stream<Path> fetch() throws IOException {
    return fetch(NIC_URLS);
  }

  /**
   * Fetches the provided urls.
   * @param urls the urls to fetch
   * @return the fetched urls
   */
  public Stream<Path> fetch(List<String> urls) {
    return urls.stream().parallel()
        .filter(url -> !url.isEmpty())
        .map(this::fetch);
  }

  /**
   * Fetches the provided url.
   * @param url the url to fetch
   * @return the fetched url
   */
  public Path fetch(String url) {
    try (InputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
      File file = File.createTempFile("nic_", ".download", new File("."));
      file.deleteOnExit();
      long size = Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      System.out.printf("Download %s (%smb)\n", url, size >> 20);
      return file.toPath();
    } catch (Exception e) {
      throw new RuntimeException("Failed to download " + url);
    }
  }
}
