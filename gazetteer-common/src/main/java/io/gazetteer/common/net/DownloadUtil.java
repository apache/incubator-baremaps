package io.gazetteer.common.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class DownloadUtil {

  public static InputStream download(String url) throws IOException {
    InputStream input = new URL(url).openConnection().getInputStream();
    return new BufferedInputStream(input);
  }

  public static InputStream downloadGZip(String url) throws IOException {
    return new GZIPInputStream(download(url));
  }

  public static InputStream downloadZip(String url) throws IOException {
    return new ZipInputStream(download(url));
  }

}
