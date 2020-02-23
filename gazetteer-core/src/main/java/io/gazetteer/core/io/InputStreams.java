package io.gazetteer.core.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InputStreams {

  public static InputStream from(String source) throws IOException {
    if (Files.exists(Paths.get(source))) {
      return Files.newInputStream(Paths.get(source));
    }
    try {
      return new BufferedInputStream(new URL(source).openConnection().getInputStream());
    } catch (MalformedURLException exception) {
      throw new IOException(String.format("Invalid source: %s", source));
    }
  }

}
