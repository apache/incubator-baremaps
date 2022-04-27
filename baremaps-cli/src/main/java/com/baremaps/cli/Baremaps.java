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
import com.baremaps.cli.geocoder.Geocoder;
import com.baremaps.cli.iploc.Iploc;
import com.baremaps.cli.ogcapi.OgcApi;
import com.baremaps.cli.pipeline.Diff;
import com.baremaps.cli.pipeline.Editor;
import com.baremaps.cli.pipeline.Execute;
import com.baremaps.cli.pipeline.Export;
import com.baremaps.cli.pipeline.Import;
import com.baremaps.cli.pipeline.Init;
import com.baremaps.cli.pipeline.Server;
import com.baremaps.cli.pipeline.Update;
import com.baremaps.cli.pipeline.Viewer;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
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
      Editor.class,
      Viewer.class,
      Server.class,
      OgcApi.class,
      Iploc.class,
      Geocoder.class
    })
public class Baremaps implements Callable<Integer> {

  @Option(
      names = {"-V", "--version"},
      versionHelp = true,
      description = "Print version info.")
  boolean version;

  @Override
  public Integer call() {
    CommandLine.usage(this, System.out);
    return 0;
  }

  public static void main(String... args) {
    // Set the log level
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      String level = "";
      if (arg.equals("--log-level") && i + 1 < args.length) {
        level = args[i + 1].strip();
      } else if (arg.startsWith("--log-level=")) {
        level = arg.substring(12).strip();
      }
      if (!"".equals(level)) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.getLevel(level));
        ctx.updateLoggers();
      }
    }

    // Execute the command
    CommandLine cmd =
        new CommandLine(new Baremaps())
            .setUsageHelpLongOptionsMaxWidth(30)
            .addMixin("options", new Options());
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
