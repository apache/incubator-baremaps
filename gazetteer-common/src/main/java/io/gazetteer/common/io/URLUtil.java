package io.gazetteer.common.io;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class URLUtil {

  public static InputStream toInputStream(URL url) throws IOException {
    return new BufferedInputStream(url.openConnection().getInputStream());
  }

  public static GZIPInputStream toGZIPInputStream(URL url) throws IOException {
    return new GZIPInputStream(toInputStream(url));
  }

  public static InputStream toZipInputStream(URL url) throws IOException {
    return new ZipInputStream(toInputStream(url));
  }

  public static String toString(URL url) throws IOException {
    return CharStreams.toString(new InputStreamReader(toInputStream(url), Charsets.UTF_8));
  }

}
