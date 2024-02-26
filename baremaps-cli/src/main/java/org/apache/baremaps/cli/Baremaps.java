/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.cli;



import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;
import org.apache.baremaps.cli.Baremaps.VersionProvider;
import org.apache.baremaps.cli.database.Database;
import org.apache.baremaps.cli.geocoder.Geocoder;
import org.apache.baremaps.cli.iploc.IpLoc;
import org.apache.baremaps.cli.map.Map;
import org.apache.baremaps.cli.tdtiles.TdTiles;
import org.apache.baremaps.cli.workflow.Workflow;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

@Command(name = "baremaps", description = "A toolkit for producing vector tiles.",
    versionProvider = VersionProvider.class, subcommands = {Workflow.class, Database.class,
        Map.class, TdTiles.class, Geocoder.class, IpLoc.class},
    sortOptions = false)
public class Baremaps implements Callable<Integer> {

  @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version info.")
  boolean version;

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
        Configurator.setRootLevel(Level.getLevel(level));
      }
    }

    // Execute the command
    CommandLine cmd = new CommandLine(new Baremaps()).setUsageHelpLongOptionsMaxWidth(30)
        .addMixin("options", new Options());
    cmd.execute(args);
  }

  @Override
  public Integer call() {
    CommandLine.usage(this, System.out);
    return 0;
  }


  static class VersionProvider implements IVersionProvider {

    public String[] getVersion() throws Exception {
      URL url = Baremaps.class.getResource("version.txt");
      if (url == null) {
        return new String[] {"No version.txt file found in the classpath."};
      }
      try (InputStream inputStream = url.openStream()) {
        Properties properties = new Properties();
        properties.load(inputStream);
        return new String[] {
            properties.getProperty("application") + " v" + properties.getProperty("version"),};
      }
    }
  }
}
