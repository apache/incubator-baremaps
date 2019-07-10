package io.gazetteer.common.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class DownloadUtil {

  public static InputStream file(URL url) throws IOException {
    return new BufferedInputStream(url.openConnection().getInputStream());
  }

  public static InputStream gzip(URL url) throws IOException {
    return new GZIPInputStream(file(url));
  }

  public static InputStream zip(URL url) throws IOException {
    return new ZipInputStream(file(url));
  }

}
