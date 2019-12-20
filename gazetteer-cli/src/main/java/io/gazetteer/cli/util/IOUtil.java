package io.gazetteer.cli.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IOUtil {

  public static URL url(String source) throws MalformedURLException {
    if (Files.exists(Paths.get(source))) {
      return Paths.get(source).toUri().toURL();
    } else {
      return new URL(source);
    }
  }

  public static InputStream input(File file) throws IOException {
    return new BufferedInputStream(new FileInputStream(file));
  }

  public static InputStream input(URL url) throws IOException {
    return new BufferedInputStream(url.openConnection().getInputStream());
  }

}
