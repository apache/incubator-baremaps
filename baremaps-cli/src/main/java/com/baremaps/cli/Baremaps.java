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

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "baremaps",
    description = "A toolkit for producing vector tiles.",
    subcommands = {
        Import.class,
        Update.class,
        Export.class,
        Serve.class,
    })
public class Baremaps implements Callable<Integer> {

  @Override
  public Integer call() {
    CommandLine.usage(new Baremaps(), System.out);
    return 0;
  }

  public static void main(String[] args) {
    CommandLine cmd = new CommandLine(new Baremaps())
        .setUsageHelpLongOptionsMaxWidth(30)
        .addMixin("logging", new Options());
    cmd.execute(args);
  }

}
