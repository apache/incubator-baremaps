/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.cli;

import com.baremaps.cli.Baremaps.VersionProvider;
import com.baremaps.osm.progress.ProgressLogger;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

@Command(
    name = "baremaps",
    description = "A toolkit for producing vector tiles.",
    versionProvider = VersionProvider.class,
    subcommands = {
        Init.class,
        Execute.class,
        Import.class,
        Update.class,
        Diff.class,
        Export.class,
        Edit.class,
        Serve.class,
    })
public class Baremaps implements Callable<Integer> {

  @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version info.")
  boolean version;

  @Override
  public Integer call() {
    CommandLine.usage(this, System.out);
    return 0;
  }

  public static void main(String[] args) {
    CommandLine cmd = new CommandLine(new Baremaps())
        .setUsageHelpLongOptionsMaxWidth(30)
        .addMixin("logging", new Options());
    cmd.execute(args);
  }

  static class VersionProvider implements IVersionProvider {
    public String[] getVersion() throws Exception {
      URL url = getClass().getResource("/version.txt");
      if (url == null) {
        return new String[] {"No version.txt file found in the classpath."};
      }
      try (InputStream inputStream = url.openStream()) {
        Properties properties = new Properties();
        properties.load(inputStream);
        return new String[] {
            properties.getProperty("application") + " v" + properties.getProperty("version"),
        };
      }
    }
  }

}
