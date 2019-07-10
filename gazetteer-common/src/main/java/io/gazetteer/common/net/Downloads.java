package io.gazetteer.common.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class Downloads {

  public static InputStream file(String url) throws IOException {
    InputStream input = new URL(url).openConnection().getInputStream();
    return new BufferedInputStream(input);
  }

  public static InputStream gzip(String url) throws IOException {
    return new GZIPInputStream(file(url));
  }

  public static InputStream zip(String url) throws IOException {
    return new ZipInputStream(file(url));
  }

}
